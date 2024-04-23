package funfit.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostResponse {

    private String userName;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;
}
