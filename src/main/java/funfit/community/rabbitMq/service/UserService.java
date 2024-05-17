package funfit.community.rabbitMq.service;

import funfit.community.rabbitMq.dto.RequestUserByEmail;
import funfit.community.rabbitMq.dto.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final RedisTemplate<String, User> redisTemplate;
    private final RabbitMqService rabbitMqService;

    public User getUserDto(String email) {
        User user = redisTemplate.opsForValue().get(email);
        if (user != null) {
            return user;
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, "community"));
    }
}
