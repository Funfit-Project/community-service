package funfit.community.utils;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.CustomJwtException;
import funfit.community.user.dto.JwtDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private final Key signingKey;
    private final JwtParser jwtParser;

    public JwtUtils(@Value("${jwt.secret}") String secretKey) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.jwtParser = Jwts.parserBuilder().setSigningKey(signingKey).build();
    }
    
    public JwtDto generateJwt(String email) {
        Claims claims = Jwts.claims()
                .setSubject(email);
        return new JwtDto(generateAccessToken(claims), generateRefreshToken(claims));
    }

    private String generateAccessToken(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60 * 60))) // 만료: 1시간
                .signWith(SignatureAlgorithm.HS256, signingKey) // 사용할 암호화 알고리즘과 secret 값
                .compact();
    }

    private String generateRefreshToken(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60 * 60 * 24))) // 만료: 24시간
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();
    }

    public void validateAccessToken(HttpServletRequest request) {
        log.info("access token 검증");
        String accessToken = getJwtFromHeader(request);
        try {
            jwtParser.parseClaimsJws(accessToken);
        } catch (ExpiredJwtException e) {
            throw new CustomJwtException(ErrorCode.EXPIRED_JWT);
        } catch (JwtException e) {
            throw new CustomJwtException(ErrorCode.INVALID_JWT);
        }
    }

    public String getEmailFromHeader(HttpServletRequest request) {
        String jwt = getJwtFromHeader(request);
        return jwtParser.parseClaimsJws(jwt).getBody().getSubject();
    }

    private String getJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.split(" ")[1];
        }
        throw new CustomJwtException(ErrorCode.REQUIRED_JWT);
    }
}
