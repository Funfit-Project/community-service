package funfit.community.utils;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.CustomJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JwtUtilsTest {

    @Autowired private JwtUtils jwtUtils;

    @Autowired private Key signingKey;

    @Test
    @DisplayName("HTTP 요청 헤더에서 이메일 추출 성공")
    public void getEmailFromHeaderSuccess() {
        // given
        Claims claims = Jwts.claims()
                .setSubject("user@naver.com");
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60 * 60))) // 만료: 1시간
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();

        // when
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization",  "Bearer " + accessToken);

        // then
        String email = jwtUtils.getEmailFromHeader(request);
        Assertions.assertThat(email).isEqualTo("user@naver.com");
    }

    @Test
    @DisplayName("HTTP 요청 헤더에서 이메일 추출 실패-토큰 누락")
    public void getEmailFromHeaderFail() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // then
        CustomJwtException exception = assertThrows(CustomJwtException.class, () -> {
            jwtUtils.getEmailFromHeader(request);
        });
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REQUIRED_JWT);
    }
}

