package funfit.community.post.service;

import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.dto.CreatePostResponse;
import funfit.community.post.dto.ReadPostResponse;
import funfit.community.post.entity.Bookmark;
import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.BookmarkRepository;
import funfit.community.post.repository.PostRepository;
import funfit.community.rabbitMq.dto.UserDto;
import funfit.community.rabbitMq.service.RabbitMqService;
import funfit.community.rabbitMq.service.UserService;
import funfit.community.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired private Key signingKey;
    @Autowired private EntityManager em;
    private PostService postService;
    private RedisTemplate<String, UserDto> userDtoRedisTemplate;
    private PostRepository postRepository;
    private BookmarkRepository bookmarkRepository;

    private String postUserEmail = "postUser@naver.com";
    private String readUserEmail = "readUser@naver.com";

    @Autowired
    public PostServiceTest(JwtUtils jwtUtils,
                           RedisTemplate<String, UserDto> userDtoRedisTemplate,
                           RabbitMqService rabbitMqService,
                           BestPostCacheService bestPostCacheService,
                           PostRepository postRepository,
                           BookmarkRepository bookmarkRepository) {
        this.postService = new PostService(jwtUtils, new StubUserService(userDtoRedisTemplate, rabbitMqService),
                bestPostCacheService, postRepository, bookmarkRepository);
        this.userDtoRedisTemplate = userDtoRedisTemplate;
        this.postRepository = postRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @BeforeEach
    public void initUserInRedis() {
        userDtoRedisTemplate.opsForValue().set(postUserEmail, new UserDto(1, postUserEmail, "postUser", "회원"));
        userDtoRedisTemplate.opsForValue().set(readUserEmail, new UserDto(2, readUserEmail, "readUser", "회원"));
    }

    @Test
    @DisplayName("게시글 등록 성공")
    public void createPostSuccess() {
        // given
        CreatePostRequest requestDto = new CreatePostRequest("title", "content", "질문");
        HttpServletRequest request = generateRequest(postUserEmail);

        // when
        CreatePostResponse responseDto = postService.create(requestDto, request);

        // then
        Assertions.assertThat(responseDto.getUserName()).isEqualTo("postUser");
        Assertions.assertThat(responseDto.getTitle()).isEqualTo("title");
        Assertions.assertThat(responseDto.getContent()).isEqualTo("content");
        Assertions.assertThat(responseDto.getCategory()).isEqualTo("질문");
    }

    @Test
    @DisplayName("게시글 단일 조회 성공")
    public void readOneSuccess() {
        // given
        creatPost();

        // when
        ReadPostResponse responseDto = postService.readOne(1);

        // then
        Post post = postRepository.findById(1l).get();
        int bookmarkCount = bookmarkRepository.findByPost(post).size();
        UserDto userDto = userDtoRedisTemplate.opsForValue().get(post.getEmail());

        Assertions.assertThat(responseDto.getUserName()).isEqualTo(userDto.getUserName());
        Assertions.assertThat(responseDto.getTitle()).isEqualTo(post.getTitle());
        Assertions.assertThat(responseDto.getContent()).isEqualTo(post.getContent());
        Assertions.assertThat(responseDto.getCategory()).isEqualTo(post.getCategory().getName());
        Assertions.assertThat(responseDto.getBookmarkCount()).isEqualTo(bookmarkCount);
        Assertions.assertThat(responseDto.getViews()).isEqualTo(post.getViews());
    }

    @Test
    @DisplayName("북마크 등록 성공")
    public void addBookmark() {
        // given
        creatPost();

        // when
        ReadPostResponse bookmark = postService.bookmark(1, generateRequest(readUserEmail));

        // then
        Assertions.assertThat(bookmark.getBookmarkCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("북마크 취소 성공")
    public void cancelBookmark() {
        // given
        creatPost();

        Post post = postRepository.findById(1l).get();

        long readUserId = userDtoRedisTemplate.opsForValue().get(readUserEmail).getUserId();
        bookmarkRepository.save(Bookmark.create(post, readUserId));

        // when
        ReadPostResponse bookmark = postService.bookmark(1, generateRequest(readUserEmail));

        // then
        Assertions.assertThat(bookmark.getBookmarkCount()).isEqualTo(0);
    }

    private void creatPost() {
        em.createNativeQuery("alter table post alter column post_id restart with 1;")
                .executeUpdate();
        Post post = Post.create(postUserEmail, "title", "content", Category.QUESTION);
        postRepository.save(post);
    }

    private HttpServletRequest generateRequest(String email) {
        Claims claims = Jwts.claims()
                .setSubject(email);
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60 * 60)))
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization",  "Bearer " + accessToken);
        return request;
    }

    private class StubUserService extends UserService {

        private RedisTemplate<String, UserDto> redisTemplate;

        public StubUserService(RedisTemplate<String, UserDto> redisTemplate, RabbitMqService rabbitMqService) {
            super(redisTemplate, rabbitMqService);
            this.redisTemplate = redisTemplate;
        }

        @Override
        public UserDto getUserDto(String email) {
            return redisTemplate.opsForValue().get(email);
        }
    }
}
