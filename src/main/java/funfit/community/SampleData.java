package funfit.community;

import funfit.community.post.entity.Category;
import funfit.community.post.entity.Post;
import funfit.community.post.repository.PostRepository;
import funfit.community.user.entity.Role;
import funfit.community.user.entity.User;
import funfit.community.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleData {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @PostConstruct
    public void initUser() {
        User user1 = User.create("user1@naver.com", "1234", "user1", Role.TRAINER, "01012341234");
        User user2 = User.create("user2@naver.com", "1234", "user2", Role.MEMBER, "01012341234");
        userRepository.save(user1);
        userRepository.save(user2);

        for (int i = 1; i <= 20; i++) {
            Post post = Post.create(user1, "제목" + i, "내용" + i, Category.WORKOUT);
            postRepository.save(post);
        }

    }
}
