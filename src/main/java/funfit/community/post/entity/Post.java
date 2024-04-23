package funfit.community.post.entity;

import funfit.community.BaseEntity;
import funfit.community.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private int views;

    public static Post create(User user, String title, String content, Category category) {
        Post post = new Post();
        post.user = user;
        post.title = title;
        post.content = content;
        post.category = category;
        return post;
    }

    public void increaseViews() {
        this.views++;
    }
}
