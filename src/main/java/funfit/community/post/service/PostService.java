package funfit.community.post.service;

import funfit.community.rabbitMq.service.UserService;
import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.dto.CreatePostResponse;
import funfit.community.post.dto.ReadPostListResponse;
import funfit.community.post.dto.ReadPostResponse;
import funfit.community.post.entity.Bookmark;
import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.BookmarkRepository;
import funfit.community.post.repository.PostRepository;
import funfit.community.rabbitMq.dto.UserDto;
import funfit.community.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final RedisTemplate redisTemplate;
    private String currentTime;
    private String previousTime;
    private static final String BEST_POSTS = "best_posts";

    @PostConstruct
    public void initCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        this.previousTime = this.currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), 0, 0).toString();
    }

    public CreatePostResponse create(CreatePostRequest createPostRequest, HttpServletRequest request) {
        String email = jwtUtils.getEmailFromHeader(request);
        UserDto userDto = userService.getUserDto(email);
        Post post = Post.create(userDto.getUserId(), userDto.getEmail(), createPostRequest.getTitle(), createPostRequest.getContent(), Category.find(createPostRequest.getCategoryName()));
        postRepository.save(post);

        return new CreatePostResponse(userDto.getUserName(), post.getTitle(), post.getContent(), post.getCategory().getName(), post.getCreatedAt());
    }

    public ReadPostResponse readOne(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        post.increaseViews();
        reflectBestPostsInCache(postId);

        int bookmarkCount = bookmarkRepository.findByPost(post).size();
        UserDto userDto = userService.getUserDto(post.getEmail());
        return new ReadPostResponse(userDto.getUserName(), post.getTitle(), post.getContent(),
                post.getCategory().getName(), post.getCreatedAt(), post.getUpdatedAt(), bookmarkCount, post.getViews());
    }

    public void reflectBestPostsInCache(long postId) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore(currentTime, String.valueOf(postId), 1);
    }

    /**
     * 정각마다 수행되는 메서드
     * - previousTime, CurrentTime 값 갱신
     * - previousTime의 best posts 10개를 추출해서 캐시
     */
    @Scheduled(cron = "0 0 * * * *")
    public void extractBestPosts() {
        updateTime();

        // previousTime가 key인 캐시 데이터 조회 및 삭제
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<String> bestPostIds = zSetOperations.reverseRange(previousTime, 0, 9);
        redisTemplate.delete(previousTime);

        // 조회한 best posts에 대한 정보를 DB에서 조회
        List<Post> bestPosts = bestPostIds.stream()
                .map(postId -> postRepository.findById(Long.valueOf(postId)).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND)))
                .toList();

        // best posts를 dto로 변환
        List<ReadPostListResponse.ReadPostResponseInList> list = bestPosts.stream()
                .map(post -> {
                    UserDto postUserDto = userService.getUserDto(post.getEmail());
                    return new ReadPostListResponse.ReadPostResponseInList(postUserDto.getUserName(), post.getTitle(), post.getCategory().getName(),
                            post.getCreatedAt(), post.getUpdatedAt(), bookmarkRepository.findByPost(post).size(), post.getViews());
                })
                .toList();
        ReadPostListResponse readPostListResponse = new ReadPostListResponse(list);

        // 기존의 best_posts 삭제 후 새로운 값 저장
        redisTemplate.delete(BEST_POSTS);
        redisTemplate.opsForList().rightPushAll(BEST_POSTS, readPostListResponse);
    }

    private void updateTime() {
        previousTime = currentTime;
        LocalDateTime now = LocalDateTime.now();
        currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond()).toString();
    }

    public ReadPostResponse bookmark(long postId, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        String email = jwtUtils.getEmailFromHeader(request);
        UserDto userDto = userService.getUserDto(email);

        Optional<Bookmark> optionalBookmark = bookmarkRepository.findByPostAndUser(post, userDto.getUserId());
        if (optionalBookmark.isPresent()) {
            Bookmark bookmark = optionalBookmark.get();
            bookmarkRepository.delete(bookmark);
        } else {
            bookmarkRepository.save(Bookmark.create(post, userDto.getUserId()));
        }

        int bookmarkCount = bookmarkRepository.findByPost(post).size();

        UserDto postUserDto = userService.getUserDto(post.getEmail());
        return new ReadPostResponse(postUserDto.getUserName(), post.getTitle(), post.getContent(),
                post.getCategory().getName(), post.getCreatedAt(), post. getUpdatedAt(), bookmarkCount, post.getViews());
    }

    public ReadPostListResponse readBestPosts() {
        return (ReadPostListResponse) redisTemplate.opsForList().leftPop(BEST_POSTS);
    }

    public Slice<ReadPostListResponse.ReadPostResponseInList> readPage(Pageable pageable) {
        Slice<Post> postsSlice = postRepository.findSliceBy(pageable);
        return postsSlice.map(post -> {
            UserDto postUserDto = userService.getUserDto(post.getEmail());
            return new ReadPostListResponse.ReadPostResponseInList(postUserDto.getUserName(), post.getTitle(),
                    post.getCategory().getName(), post.getCreatedAt(), post.getUpdatedAt(),
                    bookmarkRepository.findByPost(post).size(), post.getViews());
        });
    }
}
