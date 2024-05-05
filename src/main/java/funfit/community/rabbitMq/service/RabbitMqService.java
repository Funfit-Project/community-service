package funfit.community.rabbitMq.service;

import funfit.community.rabbitMq.dto.RequestUserByEmail;
import funfit.community.rabbitMq.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate redisTemplate;

    public UserDto requestUserByEmail(RequestUserByEmail dto) {
        Object message = rabbitTemplate.convertSendAndReceive("request_user_by_email", dto);
        log.info("response message = {}", message.toString());

        UserDto userDto = convertMessageToUserDto(message);
        redisTemplate.opsForValue().set(userDto.getEmail(), userDto);
        log.info("Redis | 사용자 정보 캐시 저장 완료");
        return userDto;
    }

    private UserDto convertMessageToUserDto(Object response) {
        LinkedHashMap map = (LinkedHashMap) response;
        UserDto dto = new UserDto();

        dto.setUserId((Integer)map.get("userId"));
        dto.setEmail((String)map.get("email"));
        dto.setPassword((String)map.get("password"));
        dto.setUserName((String)map.get("userName"));
        dto.setRoleName((String)map.get("roleName"));
        dto.setPhoneNumber((String)map.get("phoneNumber"));
        dto.setUserCode((String)map.get("userCode"));
        return dto;
    }
}
