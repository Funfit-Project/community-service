package funfit.community.post.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Bookmark {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private long id;

    @Column(nullable = false)
    private String bookmarkUserEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static Bookmark create(String bookmarkUserEmail) {
        Bookmark bookmark = new Bookmark();
        bookmark.bookmarkUserEmail = bookmarkUserEmail;
        return bookmark;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void deleteFromPost() {
        this.post.getBookmarks().remove(this);
    }
}
