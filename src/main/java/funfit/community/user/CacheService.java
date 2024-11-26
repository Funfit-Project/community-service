package funfit.community.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final AuthServiceClient authServiceClient;

    @Cacheable(value = "user", key = "#email")
    public User getUser(String email) {
        log.info("Auth 서비스로 사용자 정보 요청");
        return authServiceClient.getUserByEmail(email);
    }
}
