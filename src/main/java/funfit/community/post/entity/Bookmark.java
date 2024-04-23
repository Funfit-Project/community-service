package funfit.community.post.entity;

import funfit.community.BaseEntity;
import funfit.community.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public static Bookmark create(Post post, User user) {
        Bookmark bookmark = new Bookmark();
        bookmark.post = post;
        bookmark.user = user;
        return bookmark;
    }
}
