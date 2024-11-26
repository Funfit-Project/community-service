package funfit.community.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
public class BestPostsResponse implements Serializable {

    private String time;
    private int count;
    private List<ReadPostInListResponse> bestPostDtos;

    public BestPostsResponse(String time, List<ReadPostInListResponse> bestPostDtos) {
        this.time = time;
        this.count = bestPostDtos.size();
        this.bestPostDtos = bestPostDtos;
    }
}
