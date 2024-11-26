package funfit.community.post.service;

import funfit.community.post.entity.Like;
import funfit.community.post.repository.LikeRepository;
import funfit.community.post.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class PostLikeRedisService {

    private static final String LIKE_KEY_PREFIX = "like:";
    private static final String LIKE_ADD_KEY = "like:add";
    private static final String LIKE_REMOVE_KEY = "like:delete";
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, LikeDto> likesDtoInCacheRedisTemplate;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final SetOperations<String, String> likeUserEmailSetOps;
    private final ListOperations<String, LikeDto> likeAddOrDeleteListOps;

    public PostLikeRedisService(RedisTemplate<String, String> stringRedisTemplate, RedisTemplate<String, LikeDto> likesDtoInCacheRedisTemplate, PostRepository postRepository, LikeRepository likeRepository) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.likesDtoInCacheRedisTemplate = likesDtoInCacheRedisTemplate;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.likeUserEmailSetOps = stringRedisTemplate.opsForSet();;
        this.likeAddOrDeleteListOps = likesDtoInCacheRedisTemplate.opsForList();;
    }

    @Async
    @Transactional
    public void likePost(long postId, String email) {
        String key = LIKE_KEY_PREFIX + postId;
        if (!likeUserEmailSetOps.isMember(key, email)) {
            like(key, postId, email);
        } else {
            unlike(key, postId, email);
        }
    }

    private void unlike(String key, long postId, String email) {
        likeUserEmailSetOps.remove(key, email);
        likeAddOrDeleteListOps.leftPush(LIKE_REMOVE_KEY, new LikeDto(postId, email));
        likeAddOrDeleteListOps.remove(LIKE_ADD_KEY, 0, new LikeDto(postId, email));
    }

    private void like(String key, long postId, String email) {
        likeUserEmailSetOps.add(key, email);
        likeAddOrDeleteListOps.leftPush(LIKE_ADD_KEY, new LikeDto(postId, email));
        likeAddOrDeleteListOps.remove(LIKE_REMOVE_KEY, 0, new LikeDto(postId, email));
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeDto implements Serializable {

        private long postId;
        private String email;
    }

    /**
     * 1분 주기로 레디스의 좋아요 내역을 통해 Likes 테이블에 INSERT, Post 테이블의 likeCount UPDATE
     */
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void insertLikes() {
        log.info("게시글 좋아요 내역 DB 반영");
        Set<Long> changedPostIds = new HashSet<>();
        saveLikes(changedPostIds);
        deleteLikes(changedPostIds);
        updatePostLikeCount(changedPostIds);
    }

    private void saveLikes(Set<Long> changedPostIds) {
        for (LikeDto likeDto : likeAddOrDeleteListOps.range(LIKE_ADD_KEY, 0, -1)) {
            postRepository.findById(likeDto.getPostId())
                    .ifPresent(post -> {
                        post.addLike(Like.create(likeDto.getEmail()));
                        changedPostIds.add(post.getId());
                    });
        }
        stringRedisTemplate.delete(LIKE_ADD_KEY);
    }

    private void deleteLikes(Set<Long> changedPostIds) {
        for (LikeDto likeDto : likeAddOrDeleteListOps.range(LIKE_REMOVE_KEY, 0, -1)) {
            postRepository.findById(likeDto.getPostId())
                    .ifPresent(post -> likeRepository.findByLikeUserEmailAndPost(likeDto.getEmail(), post)
                            .ifPresent(like -> {
                                post.deleteLike(like);
                                changedPostIds.add(post.getId());
                            }));
        }
        stringRedisTemplate.delete(LIKE_REMOVE_KEY);
    }

    private void updatePostLikeCount(Set<Long> changedPostIds) {
        for (Long postId : changedPostIds) {
            postRepository.findById(postId)
                    .ifPresent(post -> {
                        Long likeCount = likeUserEmailSetOps.size(LIKE_KEY_PREFIX + post.getId());
                        post.changeLikeCount(likeCount.intValue());
                    });
        }
    }
}
