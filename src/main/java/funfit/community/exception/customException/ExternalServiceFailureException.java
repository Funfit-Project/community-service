package funfit.community.exception.customException;

import funfit.community.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExternalServiceFailureException extends RuntimeException {

    private final ErrorCode errorCode;
}
