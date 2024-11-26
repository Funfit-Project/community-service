package funfit.community.post.entity;

import funfit.community.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private long id;

    @Column(nullable = false)
    private String writerEmail;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private int views;

    private int likeCount;

    private int bookmarkCount;

    private int commentCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();

    public static Post create(String email, String title, String content, Category category) {
        Post post = new Post();
        post.writerEmail = email;
        post.title = title;
        post.content = content;
        post.category = category;
        return post;
    }

    // 연관관계 편의 메서드
    public void addComment(Comment comment) {
        this.getComments().add(comment);
        comment.setPost(this);
        this.commentCount++;
    }

    // 연관관계 편의 메서드
    public void addLike(Like like) {
        this.getLikes().add(like);
        like.setPost(this);
        this.likeCount++;
    }

    public void deleteLike(Like like) {
        this.likes.remove(like);
        likeCount--;
    }

    // 연관관계 편의 메서드
    public void addBookmark(Bookmark bookmark) {
        this.getBookmarks().add(bookmark);
        bookmark.setPost(this);
        this.bookmarkCount++;
    }

    public void deleteBookmark(Bookmark bookmark) {
        this.bookmarks.remove(bookmark);
        bookmarkCount--;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }

    public void increaseViews() {
        views++;
    }

    public void changeLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
