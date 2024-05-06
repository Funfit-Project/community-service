package funfit.community.rabbitMq.service;

import funfit.community.rabbitMq.dto.RequestUserByEmail;
import funfit.community.rabbitMq.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final RedisTemplate<String, UserDto> redisTemplate;
    private final RabbitMqService rabbitMqService;

    public UserDto getUserDto(String email) {
        UserDto userDto = redisTemplate.opsForValue().get(email);
        if (userDto != null) {
            return userDto;
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, "community"));
    }
}
