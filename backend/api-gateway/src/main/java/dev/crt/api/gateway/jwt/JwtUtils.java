package dev.crt.api.gateway.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch(ExpiredJwtException e){
            System.err.println("JWT Validation Error: Token has expired for subject: " + e.getClaims().getSubject());
            return false;
        } catch (JwtException | IllegalArgumentException e){
            System.err.println("JWT Validation Error: "+e.getMessage());
            return false;
        }
    }

    public String extractCreatorId (String token){
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return claimsJws.getBody().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
