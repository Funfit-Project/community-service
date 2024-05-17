package funfit.community.post.service;

import funfit.community.post.dto.CreatePostRequest;
import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired private PostService postService;
    @Autowired private PostRepository postRepository;

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
    @DisplayName("북마크 등록 성공")
    void addBookmarkSuccess() {
        // given
        Post post = Post.create("user@naver.com", "title", "content", Category.FREE);
        postRepository.save(post);

        // when
        postService.bookmark(post.getId(), "bookmarkUser@naver.com");

        // then
        Post savedPost = postRepository.findById(post.getId()).get();
        Assertions.assertThat(savedPost.getBookmarks().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("북마크 취소 성공")
    void cancelBookmarkSuccess() {
        // given
        Post post = Post.create("user@naver.com", "title", "content", Category.FREE);
        postRepository.save(post);

        // when
        postService.bookmark(post.getId(), "bookmarkUser@naver.com");
        postService.bookmark(post.getId(), "bookmarkUser@naver.com");

        // then
        Post savedPost = postRepository.findById(post.getId()).get();
        Assertions.assertThat(savedPost.getBookmarks().size()).isEqualTo(0);
    }
}
