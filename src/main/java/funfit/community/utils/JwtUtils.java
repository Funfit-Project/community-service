package funfit.community.utils;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.CustomJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.security.Key;

@Slf4j
@Component
public class JwtUtils {

    private final Key signingKey;
    private final JwtParser jwtParser;

    @Autowired
    public JwtUtils(Key signingKey) {
        this.signingKey = signingKey;
        this.jwtParser = Jwts.parserBuilder().setSigningKey(signingKey).build();
    }

    public String getEmailFromHeader(HttpServletRequest request) {
        String jwt = getJwtFromHeader(request);
        try {
            return jwtParser.parseClaimsJws(jwt).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            throw new CustomJwtException(ErrorCode.EXPIRED_JWT);
        } catch (JwtException e) {
            throw new CustomJwtException(ErrorCode.INVALID_JWT);
        }
    }

    private String getJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.split(" ")[1];
        }
        throw new CustomJwtException(ErrorCode.REQUIRED_JWT);
    }
}
