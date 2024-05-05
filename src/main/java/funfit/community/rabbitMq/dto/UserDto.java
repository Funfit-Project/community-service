package funfit.community.rabbitMq.dto;

import lombok.*;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private long userId;
    private String email;
    private String password;
    private String userName;
    private String roleName;
    private String phoneNumber;
    private String userCode;
}
