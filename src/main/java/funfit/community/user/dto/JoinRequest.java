package funfit.community.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class JoinRequest {

    private String email;
    private String password;
    private String name;
    private String role;
    private String phoneNumber;
}
