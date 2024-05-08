package funfit.community.post.repository;

import funfit.community.post.entity.Bookmark;
import funfit.community.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByPost(Post post);

    @Query("select b from Bookmark b " +
            "where b.post = :post " +
            "and b.userId = :userId")
    Optional<Bookmark> findByPostAndUserId(@Param("post") Post post, @Param("userId") long userId);
}
