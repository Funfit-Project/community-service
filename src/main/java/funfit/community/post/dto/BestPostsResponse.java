package funfit.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BestPostsResponse implements Serializable {

    private String time;
    private int count;
    private List<ReadPostInListResponse> bestPostDtos;
}
