package funfit.community.post.service;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.ReadPostListResponse;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.BookmarkRepository;
import funfit.community.post.repository.PostRepository;
import funfit.community.rabbitMq.dto.UserDto;
import funfit.community.rabbitMq.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BestPostCacheService {

    private final UserService userService;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, ReadPostListResponse> readPostListResponseRedisTemplate;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private String currentTime;
    private String previousTime;
    private static final String BEST_POSTS = "best_posts";

    @PostConstruct
    public void initTime() {
        LocalDateTime now = LocalDateTime.now();
        this.previousTime = this.currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), 0, 0).toString();
    }

    public void reflectPostViewsInRedis(long postId) {
        stringRedisTemplate.opsForZSet().incrementScore(currentTime, String.valueOf(postId), 1);
    }

    public ReadPostListResponse readBestPosts() {
        return readPostListResponseRedisTemplate.opsForList().leftPop(BEST_POSTS);
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void saveBestPostsDtoInRedis() {
        updateTime();

        ReadPostListResponse bestPostsDto = generateBestPostsDto();

        readPostListResponseRedisTemplate.delete(BEST_POSTS);
        readPostListResponseRedisTemplate.opsForList().set(BEST_POSTS, 0, bestPostsDto);
    }

    private ReadPostListResponse generateBestPostsDto() {
        // previousTime 캐시 데이터 조회 (조회수 높은 순으로 10개의 postId) 및 삭제
        Set<String> postIds = stringRedisTemplate.opsForZSet().reverseRange(previousTime, 0, 9);
        stringRedisTemplate.delete(previousTime);

        // dto 변환
        List<ReadPostListResponse.ReadPostResponseInList> bestPostDtos = postIds.stream()
                .map(postId -> {
                    Post post = postRepository.findById(Long.valueOf(postId))
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
                    UserDto postUserDto = userService.getUserDto(post.getEmail());
                    int bookmarkCount = bookmarkRepository.findByPost(post).size();
                    return new ReadPostListResponse.ReadPostResponseInList(postUserDto, post, bookmarkCount);
                })
                .toList();

        return new ReadPostListResponse(bestPostDtos.size(), bestPostDtos);
    }

    private void updateTime() {
        previousTime = currentTime;
        LocalDateTime now = LocalDateTime.now();
        currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond()).toString();
    }
}
