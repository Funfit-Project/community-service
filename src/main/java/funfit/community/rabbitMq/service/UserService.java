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

    private final RedisTemplate redisTemplate;
    private final RabbitMqService rabbitMqService;

    public UserDto getUserDto(String email) {
        // 캐시에 사용자가 있는지 확인 후, 없으면 MQ를 통해 받아온 후 저장
        UserDto user = (UserDto) redisTemplate.opsForValue().get(email);
        if (user != null) {
            return user;
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email));
    }
}
