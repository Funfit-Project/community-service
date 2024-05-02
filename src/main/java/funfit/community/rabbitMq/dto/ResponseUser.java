package funfit.community.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUser {

    private long userId;
    private String email;
    private String password;
    private String userName;
    private String roleName;
    private String phoneNumber;
    private String userCode;
}
