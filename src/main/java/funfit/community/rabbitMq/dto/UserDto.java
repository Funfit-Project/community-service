package funfit.community.rabbitMq.dto;

import lombok.*;

import java.io.Serializable;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {

    private long userId;
    private String email;
    private String userName;
    private String roleName;
}
