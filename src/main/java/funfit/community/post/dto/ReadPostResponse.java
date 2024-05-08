package funfit.community.post.dto;

import funfit.community.post.entity.Post;
import funfit.community.rabbitMq.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReadPostResponse {

    private String userName;
    private String title;
    private String content;
    private String category;
    private String createdAt;
    private String updatedAt;
    private int bookmarkCount;
    private int views;

    public ReadPostResponse(UserDto postUserDto, Post post, int bookmarkCount) {
        this.userName = postUserDto.getUserName();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory().getName();
        this.createdAt = post.getCreatedAt().toString();
        this.updatedAt = post.getUpdatedAt().toString();
        this.bookmarkCount = bookmarkCount;
        this.views = post.getViews();
    }
}
