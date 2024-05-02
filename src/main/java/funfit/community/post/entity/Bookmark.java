package funfit.community.post.entity;

import funfit.community.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Bookmark extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @Column(nullable = false)
    private long userId;

    public static Bookmark create(Post post, long userId) {
        Bookmark bookmark = new Bookmark();
        bookmark.post = post;
        bookmark.userId = userId;
        return bookmark;
    }
}
