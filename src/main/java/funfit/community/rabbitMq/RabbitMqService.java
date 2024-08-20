package funfit.community.rabbitMq;

import funfit.community.api.AuthServiceClient;
import funfit.community.api.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final AuthServiceClient authServiceClient;
    private final CacheManager cacheManager;

    /**
     * 회원 정보 변경 시 -> email을 통해 회원 정보 요청
     */
    @RabbitListener(queues = "edited_user_email_for_community")
    public void onMessageInEditedUserEmail(String email) {
        log.info("RabbitMQ | on message, queue name = edited_user_email_for_community, message = {}", email);
        User user = authServiceClient.getUserByEmail(email);

        cacheManager.getCache("user").put(email, user);
        log.info("로컬캐시 값 변경 = {}", user.toString());
    }
}
