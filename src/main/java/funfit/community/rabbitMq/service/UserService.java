package funfit.community.rabbitMq.service;

import funfit.community.rabbitMq.dto.MicroServiceName;
import funfit.community.rabbitMq.dto.RequestUserByEmail;
import funfit.community.rabbitMq.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    @CircuitBreaker(name = "redis", fallbackMethod = "fallback")
    public User getUserDto(String email) {
        // 레디스에서 사용자 정보 조회
        User user = redisTemplate.opsForValue().get(email);
        if (user != null) {
            return user;
        }
        return rabbitMqService.requestUserByEmail(new RequestUserByEmail(email, MicroServiceName.COMMUNITY));
    }

    private User fallback(String email, Throwable e) {
        log.error("레디스 장애로 인한 fallback 메소드 호출, {}", e.getMessage());
        // RabbitMQ를 통해 사용자 정보 요청
        return rabbitMqService.requestUserByEmailWithoutRedis(new RequestUserByEmail(email, MicroServiceName.COMMUNITY));
    }
}
