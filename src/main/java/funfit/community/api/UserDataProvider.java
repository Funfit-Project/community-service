package funfit.community.api;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataProvider {

    private final AuthServiceClient authServiceClient;

    /*
        auth 서비스 장애 또는 null -> "알수없음"으로 사용자명 반환
     */

    @CircuitBreaker(name = "auth", fallbackMethod = "fallback")
    public String getUsername(String email) {
        User user = getUser(email);
        if (user != null) {
            return user.getUserName();
        }
        return "알수없음";
    }

    @Cacheable(value = "user", key = "#email")
    public User getUser(String email) {
        // 캐시에 값이 없을 경우 아래 로직 실행
        return authServiceClient.getUserByEmail(email);
    }

    public String fallback(String email, Exception exception) {
        log.info("Auth 장애로 인한 fallback 메소드 호출: {}", exception.getMessage());
        return "알수없음";
    }
}
