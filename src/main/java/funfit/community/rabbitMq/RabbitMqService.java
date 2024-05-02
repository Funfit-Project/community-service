package funfit.community.rabbitMq;

import funfit.community.rabbitMq.dto.RequestUserByEmail;
import funfit.community.rabbitMq.dto.ResponseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate redisTemplate;

    public void requestUserByEmail(RequestUserByEmail dto) {
        log.info("RabbitMQ| request user by email, user email = {}", dto.getEmail());
        rabbitTemplate.convertAndSend("user_request_by_email", dto);
        log.info("RabbitMQ| success send messages");
    }

    @RabbitListener(queues = "user")
    public void onMessageInUser(final ResponseUser dto) {
        log.info("RabbitMQ| on message in user, user = {}", dto.toString());
        redisTemplate.opsForValue().set(dto.getEmail(), dto);
        log.info("사용자 정보 캐시에 저장 완료");
    }
}
