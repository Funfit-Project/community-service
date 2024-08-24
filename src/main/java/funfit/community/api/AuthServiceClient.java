package funfit.community.api;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.ExternalServiceFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", fallbackFactory = AuthServiceClient.FallbackFactory.class, primary = true)
public interface AuthServiceClient {

    @GetMapping("/feignClient/user/community")
    User getUserByEmail(@RequestParam(name = "email") String email);

    @Component
    @Slf4j
    class Fallback implements AuthServiceClient {

        @Override
        public User getUserByEmail(String email) {
            log.info("throw ExternalServiceFailureException");
            throw new ExternalServiceFailureException(ErrorCode.UNAVAILABLE_AUTH_SERVICE);
        }
    }

    @Slf4j
    @Component
    @RequiredArgsConstructor
    class FallbackFactory implements org.springframework.cloud.openfeign.FallbackFactory<Fallback> {

        private final Fallback fallback;

        @Override
        public Fallback create(Throwable cause) {
            log.info(cause.getMessage());
            return fallback;
        }
    }
}
