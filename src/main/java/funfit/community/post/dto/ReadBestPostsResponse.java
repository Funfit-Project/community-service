package funfit.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "best_posts")
public class ReadBestPostsResponse implements Serializable {

    private String time;
    private int count;
    private List<ReadPostInListResponse> bestPostDtos;
}
