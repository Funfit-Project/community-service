package funfit.community.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadPostListResponse {

    private List<ReadPostResponseInList> data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadPostResponseInList {
        private String userName;
        private String title;
        private String category;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int bookmarkCount;
        private int views;
    }
}
