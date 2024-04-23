package funfit.community.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class JwtDto {

    private String accessToken;
    private String refreshToken;
}
