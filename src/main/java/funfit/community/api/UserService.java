package funfit.community.api;

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
    private final AuthServiceClient authServiceClient;

    @CircuitBreaker(name = "redis", fallbackMethod = "fallback")
    public User getUserDto(String email) {
        User user = redisTemplate.opsForValue().get(email);
        if (user != null) {
            return user;
        }
        return authServiceClient.getUserByEmail(email);
    }

    private User fallback(String email, Throwable e) {
        log.error("레디스 장애로 인한 fallback 메소드 호출, {}", e.getMessage());
        return authServiceClient.getUserByEmail(email);
    }
}
