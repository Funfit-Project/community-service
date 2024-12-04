package funfit.community.post.service;

import funfit.community.post.entity.Like;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.LikeRepository;
import funfit.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
        try {
            likeRepository.save(Like.create(email, post));
        } catch (DataIntegrityViolationException e) {
            log.info("Like 엔티티 중복 저장으로 인한 DataIntegrityViolationException 예외 발생");
        }
    }

    private void unlike(SetOperations<String, String> setOps, String key, Post post, String email) {
        setOps.remove(key, email);
        try {
            likeRepository.findByLikeUserEmailAndPost(email, post)
                    .ifPresent(like ->  likeRepository.delete(like));
        } catch (ObjectOptimisticLockingFailureException e) {
            log.info("catch ObjectOptimisticLockingFailureException!");
        }
    }

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void updateLikeCount() {
        log.info("updateLikeCount(게시글 좋아요 수 갱신) 스케줄링 작업 시작");
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
        log.info("updateLikeCount(게시글 좋아요 수 갱신) 스케줄링 작업 종료");
    }
}
