package funfit.community;

import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import funfit.community.post.service.PostService;
import funfit.community.query.PostQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class Init {

    private final PostService postService;
    private final PostQueryService postQueryService;
    private final PostRepository postRepository;

    Random random = new Random();

    @EventListener(value = ApplicationReadyEvent.class)
    public void initPost() {


        for (int i = 1; i <= 1000; i++) {

            int id = random.nextInt(1000) + 1;
            String email = "user" + id + "@naver.com";

            // 저장
            Post post = Post.create(email, "title" + i, "content" + i, Category.FREE);
            postRepository.save(post);
        }
    }

    @Transactional
    public void likeOne() {
        // 10,000명의 회원이 모두 게시글 1번에 좋아요 누름
        for (int i = 1; i <= 10000; i++) {
            String email = "user" + i + "@naver.com";
            postService.likePost(1, email);
        }
    }

    @Transactional
    public void like() {
        // 좋아요
        for (int i = 1; i <= 1000; i++) {
            String email = "user" + i + "@naver.com";
            int id = random.nextInt(100000) + 1;
            postService.likePost(id, email);
        }
    }
}
