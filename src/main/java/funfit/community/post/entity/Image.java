package funfit.community.post.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private long id;

    @Column(nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static Image create(String url) {
        Image image = new Image();
        image.url = url;
        return image;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
