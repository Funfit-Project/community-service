package funfit.community.user;

import lombok.*;

import java.io.Serializable;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private long userId;
    private String email;
    private String userName;
    private String roleName;
}
