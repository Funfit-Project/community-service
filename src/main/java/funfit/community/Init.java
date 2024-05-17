package funfit.community;

import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Init {

    private final PostRepository postRepository;

    @PostConstruct
    public void initPost() {
        for (int i = 1; i <= 1000; i++) {
            Post post = Post.create("user" + i + "@naver.com", "title" + i, "content" + i, Category.QUESTION);
            postRepository.save(post);
        }
    }
}
