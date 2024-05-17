package funfit.community.post.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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

    public void setPost(Post post) {
        this.post = post;
    }
}
