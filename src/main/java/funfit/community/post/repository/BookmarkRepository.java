package funfit.community.post.repository;

import funfit.community.post.entity.Bookmark;
import funfit.community.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByPostAndBookmarkUserEmail(Post post, String bookmarkUserEmail);
}
