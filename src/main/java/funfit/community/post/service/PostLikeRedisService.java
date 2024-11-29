package funfit.community.post.service;

import funfit.community.post.entity.Like;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.LikeRepository;
import funfit.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostLikeRedisService {

    private static final String LIKE_KEY_PREFIX = "post:like:";
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    @Async
    public void likePost(String email, Post post) {
        SetOperations<String, String> setOps = stringRedisTemplate.opsForSet();
        String key = LIKE_KEY_PREFIX + post.getId();
        if (!setOps.isMember(key, email)) {
            like(setOps, key, post, email);
        } else {
            unlike(setOps, key, post, email);
        }
    }

    private void like(SetOperations<String, String> setOps, String key, Post post, String email) {
        setOps.add(key, email);
        Like like = Like.create(email);
        like.setPost(post);
        likeRepository.save(like);
    }

    private void unlike(SetOperations<String, String> setOps, String key, Post post, String email) {
        setOps.remove(key, email);
        likeRepository.findByLikeUserEmailAndPost(email, post)
                        .ifPresent(like ->  likeRepository.delete(like));
    }

    /**
     * 1분 주기로 레디스의 좋아요 내역을 통해 Likes 테이블에 INSERT, Post 테이블의 likeCount UPDATE
     */
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void updateLikeCount() {
        log.info("게시글 좋아요 내역 DB 반영");

        Set<String> keys = new HashSet<>();
        Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory().getConnection()
                .scan(ScanOptions.scanOptions().match("post:like:*").count(100).build());

        while (cursor.hasNext()) {
            String key = new String(cursor.next());
            keys.add(key);
        }

        for (String key : keys) {
            long postId = Long.parseLong(key.split(":")[2]);
            Long likeCount = stringRedisTemplate.opsForSet().size(key);
            postRepository.findById(postId)
                    .ifPresent(post -> post.changeLikeCount(likeCount.intValue()));
        }
    }
}
