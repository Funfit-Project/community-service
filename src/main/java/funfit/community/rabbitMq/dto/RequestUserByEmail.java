package funfit.community.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestUserByEmail {

    private String email;
    private String responseQueue; // 응답받을 큐 이름
}
