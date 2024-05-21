package funfit.community.post.service;

import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Transactional
@SpringBootTest
@Rollback(value = false)
class PostServiceTest {

    @Autowired private PostService postService;
    @Autowired private PostRepository postRepository;
    @Autowired private InitService initService;

    @TestConfiguration
    static class TestConfig {

        @Autowired PostRepository postRepository;

        @Bean
        InitService initService() {
            return new InitService(postRepository);
        }
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void createPostSuccess() {
        // given
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("url1");
        imageUrls.add("url2");
        CreatePostRequest requestDto = new CreatePostRequest("title", "content", "질문", imageUrls);

        // when
        long postId = postService.create(requestDto, "user@naver.com");

        // then
        Assertions.assertThat(postRepository.findById(postId)).isPresent();
        Post post = postRepository.findById(postId).get();

        Assertions.assertThat(post.getWriterEmail()).isEqualTo("user@naver.com");
        Assertions.assertThat(post.getTitle()).isEqualTo("title");
        Assertions.assertThat(post.getContent()).isEqualTo("content");
        Assertions.assertThat(post.getCategory()).isEqualTo(Category.QUESTION);
    }

    @Test
    @DisplayName("조회수 증가 성공")
    void increaseViewsSuccess() {
        // given
        Post post = Post.create("user@naver.com", "title", "content", Category.FREE);
        postRepository.save(post);

        // when
        postService.increaseViews(post.getId());

        // then
        Post savedPost = postRepository.findById(post.getId()).get();
        Assertions.assertThat(savedPost.getViews()).isEqualTo(1);
    }

    @Test
    @DisplayName("북마크 등록 성공-한 사람이 동시에 여러번 좋아요를 눌러도 중복 저장되지 않아야 한다.")
    void addBookmarkSuccessByOneUser() throws InterruptedException {
        long savedPostId = initService.initPost();

        int numberOfThreads = 11;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                postService.bookmark(savedPostId, "bookmarkUser@naver.com");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();

        Post savedPost = postRepository.findById(savedPostId).get();
        Assertions.assertThat(savedPost.getBookmarks().size()).isEqualTo(1);
        Assertions.assertThat(savedPost.getBookmarkCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("북마크 취소 성공-한 사람이 동시에 여러번 좋아요를 눌러도 중복 저장되지 않아야 한다.")
    void deleteBookmarkSuccessByOneUser() throws InterruptedException {
        long savedPostId = initService.initPost();

        int numberOfThreads = 10;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                postService.bookmark(savedPostId, "bookmarkUser@naver.com");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();

        Post savedPost = postRepository.findById(savedPostId).get();
        Assertions.assertThat(savedPost.getBookmarks().size()).isEqualTo(0);
        Assertions.assertThat(savedPost.getBookmarkCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("북마크 등록 성공-여러 사람이 동시에 북마크를 해도 값이 덮어씌워지지 않아야 한다.")
    void addBookmarkSuccessByUsers() throws InterruptedException {
        long savedPostId = initService.initPost();

        int numberOfThreads = 10;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 1; i <= numberOfThreads; i++) {
            String email = "user" + i + "@naver.com";
            executorService.execute(() -> {
                postService.bookmark(savedPostId, email);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();

        Post savedPost = postRepository.findById(savedPostId).get();
        Assertions.assertThat(savedPost.getBookmarks().size()).isEqualTo(10);
        Assertions.assertThat(savedPost.getBookmarkCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("좋아요 등록 성공-한 사람이 동시에 여러번 좋아요를 눌러도 중복 저장되지 않아야 한다.")
    void likePostSuccessByOneUser() throws InterruptedException {
        long savedPostId = initService.initPost();

        int numberOfThreads = 11;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                postService.likePost(savedPostId, "likeUser@naver.com");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();

        Post savedPost = postRepository.findById(savedPostId).get();
        Assertions.assertThat(savedPost.getLikes().size()).isEqualTo(1);
        Assertions.assertThat(savedPost.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 등록 성공-여러 사람이 동시에 좋아요를 눌러도 값이 덮어씌워지지 않아야 한다.")
    void likePostSuccessByUsers() throws InterruptedException {
        long savedPostId = initService.initPost();

        int numberOfThreads = 10;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 1; i <= numberOfThreads; i++) {
            String email = "user" + i + "@naver.com";
            executorService.execute(() -> {
                postService.likePost(savedPostId, email);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();

        Post savedPost = postRepository.findById(savedPostId).get();
        Assertions.assertThat(savedPost.getLikes().size()).isEqualTo(10);
        Assertions.assertThat(savedPost.getLikeCount()).isEqualTo(10);
    }

    @RequiredArgsConstructor
    static class InitService {

        private final PostRepository postRepository;

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public long initPost() {
            Post post = Post.create("user@naver.com", "title", "content", Category.FREE);
            postRepository.save(post);
            return post.getId();
        }
    }
}
