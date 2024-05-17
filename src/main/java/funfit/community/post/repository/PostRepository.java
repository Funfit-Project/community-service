package funfit.community.post.repository;

import funfit.community.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p " +
            "left join Like l on l.post.id = p.id " +
            "group by p " +
            "order by count(l) desc")
    Slice<Post> findOrderByLikeCount(Pageable pageable);
}
