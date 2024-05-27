package funfit.community.exception.customException;

public class RabbitMqException extends RuntimeException {

    public RabbitMqException(String message) {
        super(message);
    }
}
