package funfit.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    private String title;
    private String content;
    private String categoryName;
    private List<String> imageUrls;
}
