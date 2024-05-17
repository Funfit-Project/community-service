package funfit.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadPostResponse implements Serializable {

    private String userName;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int bookmarkCount;
    private int likeCount;
    private int views;
    private List<String> imageUrls;
}
