package funfit.community.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 데이터를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다."),

    // not found
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // user
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    NOT_FOUND_EMAIL(HttpStatus.BAD_REQUEST, "가입되지 않은 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 패스워드입니다."),

    // relationship
    REGISTER_ONLY_FOR_MEMBER(HttpStatus.BAD_REQUEST, "회원만 트레이너를 추가할 수 있습니다."),
    INVALID_USER_CODE(HttpStatus.BAD_REQUEST, "잘못된 회원코드입니다."),
    DUPLICATED_RELATIONSHIP(HttpStatus.BAD_REQUEST, "이미 등록된 트레이너입니다"),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "잘못된 사용자 역할입니다."),

    // post
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "잘못된 카테고리입니다."),
    NOT_FOUND_POST(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),


    // jwt
    EXPIRED_JWT(HttpStatus.BAD_REQUEST, "만료된 토큰입니다."),
    INVALID_JWT(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."),
    REQUIRED_JWT(HttpStatus.BAD_REQUEST, "토큰은 필수입니다."),

    // unauthorized
    ONLY_TRAINER(HttpStatus.UNAUTHORIZED, "트레이너만 접근 가능합니다."),

    INVALID_BEST_POSTS_TIME(HttpStatus.BAD_REQUEST, "인기글 시간은 한시간 단위입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
