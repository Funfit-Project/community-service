package funfit.community.post.service;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.BestPostsResponse;
import funfit.community.post.dto.ReadPostInListResponse;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import funfit.community.user.UserDataProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class BestPostCacheService {

    private final UserDataProvider userDataProvider;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, BestPostsResponse> bestPostsRedisTemplate;
    private final PostRepository postRepository;
    private static final String POST_VIEWS_PREFIX = "views:";
    private static final String BEST_POSTS_PREFIX = "best_posts:";
    private String currentTime;
    private String previousTime;

    @PostConstruct
    public void initTime() {
        LocalDateTime now = LocalDateTime.now();
        this.previousTime = this.currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), 0, 0).toString();
    }

    public void reflectPostViewsInCache(long postId) {
        stringRedisTemplate.opsForZSet().incrementScore(POST_VIEWS_PREFIX + currentTime, String.valueOf(postId), 1);
    }

    public BestPostsResponse readBestPosts(LocalDateTime time) {
        return bestPostsRedisTemplate.opsForValue().get(BEST_POSTS_PREFIX + time);
    }

    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각마다 실행
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 1.5, maxDelay = 5000))
    public void cacheBestPostsDto() {
        log.info("cacheBestPostsDto(인기글 추출) 스케줄링 작업 시작");

        updateTime();

        Set<String> postIds = stringRedisTemplate.opsForZSet().reverseRange(POST_VIEWS_PREFIX + previousTime, 0, 9);
        stringRedisTemplate.delete(previousTime);

        BestPostsResponse bestPostsResponse = new BestPostsResponse(previousTime, getPostResponse(postIds));
        bestPostsRedisTemplate.opsForValue().set(BEST_POSTS_PREFIX + previousTime, bestPostsResponse);
        log.info("cacheBestPostsDto(인기글 추출) 스케줄링 작업 종료");
    }

    private List<ReadPostInListResponse> getPostResponse(Set<String> postIds) {
        return postIds.stream()
                .map(postId -> {
                    Post post = postRepository.findById(Long.valueOf(postId))
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
                    String username = userDataProvider.getUsername(post.getWriterEmail());
                    return new ReadPostInListResponse(post.getTitle(), username, post.getCategory().getName(),
                            post.getCreatedAt().toString(), post.getUpdatedAt().toString(),
                            post.getComments().size(), post.getLikes().size(), post.getBookmarks().size(), post.getViews());
                })
                .toList();
    }

    private void updateTime() {
        previousTime = currentTime;
        LocalDateTime now = LocalDateTime.now();
        currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), 0, 0).toString();
    }
}
