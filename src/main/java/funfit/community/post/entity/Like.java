package funfit.community.post.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "like_user_email"}))
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private long id;

    @Column(nullable = false)
    private String likeUserEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static Like create(String likeUserEmail) {
        Like like = new Like();
        like.likeUserEmail = likeUserEmail;
        return like;
    }

    public static Like create(String likeUserEmail, Post post) {
        Like like = new Like();
        like.likeUserEmail = likeUserEmail;
        like.post = post;
        return like;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void deleteFromPost() {
        this.post.getLikes().remove(this);
    }
}
