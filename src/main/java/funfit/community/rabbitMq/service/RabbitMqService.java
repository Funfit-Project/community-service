package funfit.community.rabbitMq.service;

import funfit.community.rabbitMq.dto.RequestUserByEmail;
import funfit.community.rabbitMq.dto.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, User> redisTemplate;

    public User requestUserByEmail(RequestUserByEmail dto) {
        Object message = rabbitTemplate.convertSendAndReceive("request_user_by_email", dto);
        log.info("response message = {}", message.toString());

        User user = convertMessageToUserDto(message);
        redisTemplate.opsForValue().set(user.getEmail(), user);
        log.info("Redis | 사용자 정보 캐시 저장 완료");
        return user;
    }

    private User convertMessageToUserDto(Object response) {
        LinkedHashMap map = (LinkedHashMap) response;
        User dto = new User();

        dto.setUserId((Integer)map.get("userId"));
        dto.setEmail((String)map.get("email"));
        dto.setUserName((String)map.get("userName"));
        dto.setRoleName((String)map.get("roleName"));
        return dto;
    }

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
