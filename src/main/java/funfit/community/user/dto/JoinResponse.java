package funfit.community.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class JoinResponse {

    private String email;
    private String name;
    private String role;
}
