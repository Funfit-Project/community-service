package funfit.community.post.repository;

import funfit.community.post.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p " +
            "left join fetch p.bookmarks " +
            "where p.id = :postId")
    Optional<Post> findByIdWithBookmarkWithLock(@Param("postId") long postId);
}
