package funfit.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReadBestPostsResponse {

    private String time;
    private int count;
    private List<ReadPostInListResponse> bestPostDtos;
}
