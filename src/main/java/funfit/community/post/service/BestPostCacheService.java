package funfit.community.post.service;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.BestPostsResponse;
import funfit.community.post.dto.ReadPostInListResponse;
import funfit.community.post.entity.BestPosts;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import funfit.community.api.UserDataProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    private final RedisTemplate<String, BestPosts> bestPostsRedisTemplate;
    private final PostRepository postRepository;
    private String currentTime;
    private String previousTime;
    private static final String BEST_POSTS_PREFIX = "best_posts_";

    @PostConstruct
    public void initTime() {
        LocalDateTime now = LocalDateTime.now();
        this.previousTime = this.currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), 0, 0).toString();
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "fallback")
    public void reflectPostViewsInRedis(long postId) {
        stringRedisTemplate.opsForZSet().incrementScore(currentTime, String.valueOf(postId), 1);
    }

    public void fallback(long postId, Exception exception) {
        log.info("레디스 장애로 인한 fallback 메소드 호출: {}", exception.getMessage());
    }

    public BestPostsResponse readBestPosts(LocalDateTime time) {
        // 레디스에서 인기글 조회
        BestPosts bestPosts = bestPostsRedisTemplate.opsForValue().get(BEST_POSTS_PREFIX + time);
        return new BestPostsResponse(bestPosts.getTime(), bestPosts.getCount(), bestPosts.getBestPostDtos());
    }

//    @Scheduled(cron = "0 * * * * *") // 1분마다 실행
    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각마다 실행
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 1.5, maxDelay = 5000))
    public void saveBestPostsDtoInRedis() {
        log.info("스케줄링 작업 수행: 인기글 추출 작업");
        updateTime();
        log.info("update time: {} -> {}", previousTime, currentTime);

        // previousTime 변수에 해당하는 key에 저장된 게시글 id들 중 조회수가 높은 10개의 id를 조회 및 삭제
        Set<String> postIds = stringRedisTemplate.opsForZSet().reverseRange(previousTime, 0, 9);
        stringRedisTemplate.delete(previousTime);

        // 게시글 id에 해당하는 데이터를 DB에서 조회한 후 dto 변환
        List<ReadPostInListResponse> bestPostDtos = postIds.stream()
                .map(postId -> {
                    Post post = postRepository.findById(Long.valueOf(postId))
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
                    String postUserName = userDataProvider.getUserName(post.getWriterEmail());
                    return new ReadPostInListResponse(post.getTitle(), postUserName, post.getCategory().getName(),
                            post.getCreatedAt().toString(), post.getUpdatedAt().toString(),
                            post.getComments().size(), post.getLikes().size(), post.getBookmarks().size(), post.getViews());
                })
                .toList();

        // 레디스에 인기글 저장
        BestPosts bestPosts = new BestPosts(previousTime, bestPostDtos);
        bestPostsRedisTemplate.opsForValue().set(BEST_POSTS_PREFIX + previousTime, bestPosts);
    }

    private void updateTime() {
        previousTime = currentTime;
        LocalDateTime now = LocalDateTime.now();
        currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), 0, 0).toString();
    }
}
