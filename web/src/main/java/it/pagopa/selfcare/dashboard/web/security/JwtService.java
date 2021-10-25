package it.pagopa.selfcare.dashboard.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Common helper methods to work with JWT
 */
@Slf4j
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

//    public String generateJwtToken(Authentication authentication) {
//
//        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
//
//        return Jwts.builder()
//                .setSubject((userPrincipal.getUsername()))
//                .setIssuedAt(new Date())
//                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
//                .signWith(SignatureAlgorithm.HS512, jwtSecret)
//                .compact();
//    }

    public String getSubjectFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }


    public Optional<Claims> getClaims(String token) {
        Optional<Claims> claims = Optional.empty();
        try {
            claims = Optional.of(Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return claims;
    }

    public boolean validateJwtToken(String authToken) {
        boolean valid = false;
        if (authToken != null) {
            try {
                Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
                valid = true;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return valid;
    }
}
