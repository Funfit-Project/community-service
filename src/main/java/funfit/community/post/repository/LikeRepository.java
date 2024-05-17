package funfit.community.post.repository;

import funfit.community.post.entity.Like;
import funfit.community.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByPostAndLikeUserEmail(Post post, String email);
}
