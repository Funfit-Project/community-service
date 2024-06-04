package funfit.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReadPostInListResponse implements Serializable {

    private String title;
    private String userName;
    private String category;
    private String createdAt;
    private String updatedAt;
    private int commentCount;
    private int likeCount;
    private int bookmarkCount;
    private int views;
}
