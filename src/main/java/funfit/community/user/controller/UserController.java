package funfit.community.user.controller;

import funfit.community.responseDto.SuccessResponse;
import funfit.community.user.dto.JoinRequest;
import funfit.community.user.dto.JoinResponse;
import funfit.community.user.dto.JwtDto;
import funfit.community.user.dto.LoginRequest;
import funfit.community.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/community/join")
    public ResponseEntity join(@RequestBody JoinRequest joinRequest) {
        JoinResponse joinResponse = userService.join(joinRequest);
        return ResponseEntity.status(HttpStatus.OK)
                        .body(new SuccessResponse("사용자 회원가입 성공", joinResponse));
    }

    @PostMapping("/community/login")
    public ResponseEntity login(@RequestBody LoginRequest loginRequest) {
        JwtDto jwtDto = userService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("사용자 로그인 성공", jwtDto));
    }
}
