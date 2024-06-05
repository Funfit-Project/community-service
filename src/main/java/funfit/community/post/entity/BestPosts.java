package funfit.community.post.entity;

import funfit.community.post.dto.ReadPostInListResponse;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = "best_posts")
@Getter
@NoArgsConstructor
public class BestPosts implements Serializable {

    @Id
    private String id;
    private String time;
    private int count;
    private List<ReadPostInListResponse> bestPostDtos;

    public BestPosts(String time, List<ReadPostInListResponse> bestPostDtos) {
        this.time = time;
        this.count = bestPostDtos.size();
        this.bestPostDtos = bestPostDtos;
    }
}
