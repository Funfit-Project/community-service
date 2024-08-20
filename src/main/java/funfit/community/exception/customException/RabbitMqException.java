package funfit.community.exception.customException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RabbitMqException extends RuntimeException {

    public RabbitMqException(String message) {
        super(message);
    }
}
