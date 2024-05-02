package funfit.community.post.service;

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
import funfit.community.rabbitMq.RabbitMqService;
import funfit.community.rabbitMq.dto.RequestUserByEmail;
import funfit.community.rabbitMq.dto.ResponseUser;
import funfit.community.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
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
public class PostService {

    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final JwtUtils jwtUtils;
    private final RedisTemplate redisTemplate;
    private final RabbitMqService rabbitMqService;
    private String currentTime;
    private String previousTime;
    private static final String BEST_POSTS = "best_posts";

    public PostService(PostRepository postRepository, JwtUtils jwtUtils,
                       BookmarkRepository bookmarkRepository,
                       RabbitMqService rabbitMqService,
                       RedisTemplate redisTemplate) {
        this.postRepository = postRepository;
        this.jwtUtils = jwtUtils;
        this.bookmarkRepository = bookmarkRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitMqService = rabbitMqService;

        LocalDateTime now = LocalDateTime.now();
        this.currentTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond()).toString();
        this.currentTime = this.previousTime = currentTime;
    }

    public CreatePostResponse create(CreatePostRequest createPostRequest, HttpServletRequest request) {
        String email = jwtUtils.getEmailFromHeader(request);
        // 캐시에 사용자가 있는지 확인 후, 없으면 MQ를 통해 받아온 후 저장
        ResponseUser user = (ResponseUser) redisTemplate.opsForValue().get(email);
        if (user == null) {
            rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, "user"));
        }
        // 캐시에서 사용자 정보 받아오기
        ResponseUser responseUser = (ResponseUser) redisTemplate.opsForValue().get(email);

        Post post = Post.create(responseUser.getUserId(), responseUser.getUserName(), createPostRequest.getTitle(), createPostRequest.getContent(), Category.find(createPostRequest.getCategoryName()));
        postRepository.save(post);
        return new CreatePostResponse(post.getUsername(), post.getTitle(), post.getContent(), post.getCategory().getName(), post.getCreatedAt());
    }

    public ReadPostResponse readOne(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        post.increaseViews();
        reflectBestPostsInCache(postId);

        int bookmarkCount = bookmarkRepository.findByPost(post).size();

        return new ReadPostResponse(post.getUsername(), post.getTitle(), post.getContent(),
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
                .map(post -> new ReadPostListResponse.ReadPostResponseInList(post.getUsername(), post.getTitle(), post.getCategory().getName(),
                        post.getCreatedAt(), post.getUpdatedAt(), bookmarkRepository.findByPost(post).size(), post.getViews()))
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
        // 캐시에 사용자가 있는지 확인 후, 없으면 MQ를 통해 받아온 후 저장
        ResponseUser user = (ResponseUser) redisTemplate.opsForValue().get(email);
        if (user == null) {
            rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, "user"));
        }
        // 캐시에서 사용자 정보 받아오기
        ResponseUser responseUser = (ResponseUser) redisTemplate.opsForValue().get(email);

        Optional<Bookmark> optionalBookmark = bookmarkRepository.findByPostAndUser(post, responseUser.getUserId());
        if (optionalBookmark.isPresent()) {
            Bookmark bookmark = optionalBookmark.get();
            bookmarkRepository.delete(bookmark);
        } else {
            bookmarkRepository.save(Bookmark.create(post, responseUser.getUserId()));
        }

        int bookmarkCount = bookmarkRepository.findByPost(post).size();
        return new ReadPostResponse(post.getUsername(), post.getTitle(), post.getContent(),
                post.getCategory().getName(), post.getCreatedAt(), post. getUpdatedAt(), bookmarkCount, post.getViews());
    }

    public ReadPostListResponse readBestPosts() {
        return (ReadPostListResponse) redisTemplate.opsForList().leftPop(BEST_POSTS);
    }

    public Slice<ReadPostListResponse.ReadPostResponseInList> readPage(Pageable pageable) {
        Slice<Post> postsSlice = postRepository.findSliceBy(pageable);
        return postsSlice.map(post -> new ReadPostListResponse.ReadPostResponseInList(post.getUsername(), post.getTitle(),
                post.getCategory().getName(), post.getCreatedAt(), post.getUpdatedAt(),
                bookmarkRepository.findByPost(post).size(), post.getViews()));
    }
}
