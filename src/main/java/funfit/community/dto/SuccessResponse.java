package funfit.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SuccessResponse {

    private String message;
    private Object data;

    public SuccessResponse(String message) {
        this.message = message;
    }
}
