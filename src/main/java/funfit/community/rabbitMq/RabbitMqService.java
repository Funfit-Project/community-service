package funfit.community.rabbitMq;

import funfit.community.api.AuthServiceClient;
import funfit.community.api.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RedisTemplate<String, User> redisTemplate;
    private final AuthServiceClient authServiceClient;

    @RabbitListener(queues = "edited_user_email_for_community")
    public void onMessageInEditedUserEmail(String email) {
        log.info("RabbitMQ | on message, queue name = edited_user_email_for_community, message = {}", email);
        User user = authServiceClient.getUserByEmail(email);
        redisTemplate.opsForValue().set(user.getEmail(), user);
    }
}
