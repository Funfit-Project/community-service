package funfit.community.user.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity(name = "users")
@Getter
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    private String phoneNumber;

    private String userCode;

    public static User create(String email, String password, String name, Role role, String phoneNumber) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.name = name;
        user.role = role;
        if (role == Role.TRAINER) {
            user.userCode = createUserCode();
        }
        user.phoneNumber = phoneNumber;
        return user;
    }

    private static String createUserCode() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "").substring(0, 8);
    }

    public boolean isTrainer() {
        return this.role == Role.TRAINER;
    }

    public boolean isMember() {
        return this.role == Role.MEMBER;
    }
}
