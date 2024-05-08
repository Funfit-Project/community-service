package funfit.community.post.dto;

import funfit.community.post.entity.Post;
import funfit.community.rabbitMq.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReadPostListResponse implements Serializable {

    private int count;
    private List<ReadPostResponseInList> data;

    @Getter
    @NoArgsConstructor
    public static class ReadPostResponseInList implements Serializable {

        private String userName;
        private String title;
        private String category;
        private String createdAt;
        private String updatedAt;
        private int bookmarkCount;
        private int views;

        public ReadPostResponseInList(UserDto postUserDto, Post post, int bookmarkCount) {
            this.userName = postUserDto.getUserName();
            this.title = post.getTitle();
            this.category = post.getCategory().getName();
            this.createdAt = post.getCreatedAt().toString();
            this.updatedAt = post.getUpdatedAt().toString();
            this.bookmarkCount = bookmarkCount;
            this.views = post.getViews();
        }
    }
}
