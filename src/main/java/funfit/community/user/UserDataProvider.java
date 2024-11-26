package funfit.community.user;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataProvider {

    private final CacheService cacheService;

    @CircuitBreaker(name = "auth", fallbackMethod = "fallback")
    public String getUsername(String email) {
        User user = cacheService.getUser(email);
        if (user != null) {
            return user.getUserName();
        }
        return "알수없음";
    }

    public String fallback(String email, Exception exception) {
        log.info("Auth 장애로 인한 fallback 메소드 호출: {}", exception.getMessage());
        return "알수없음";
    }
}
