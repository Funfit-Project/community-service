package funfit.community.rabbitMq;

import funfit.community.api.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RedisTemplate<String, User> redisTemplate;

    @RabbitListener(queues = "edited_user_id_for_community")
    public void onMessageInEditedUserIdForCommunity(long userId) {
        log.info("RabbitMQ | on message in edited_user_id_for_community queue, message = {}", userId);
        String url = "http://localhost:8081/userInfo/community/" + userId;
        RestTemplate restTemplate = new RestTemplate();
        User user = restTemplate.getForObject(url, User.class);
        redisTemplate.opsForValue().set(user.getEmail(), user);
        log.info("Redis | 사용자 정보 수정사항 반영 완료");
    }
}
