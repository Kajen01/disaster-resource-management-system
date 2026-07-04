package com.drms.apigateway.security;

import com.drms.apigateway.config.RouteSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Key signingKey;

    public JwtService(RouteSecurityProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.jwtSecret()));
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractRole(String token) {
        return parse(token).get("role", String.class);
    }

    public String extractSubject(String token) {
        return parse(token).getSubject();
    }

    public String extractUserId(String token) {
        Object userId = parse(token).get("userId");
        return userId == null ? "" : String.valueOf(userId);
    }

    public boolean hasRequiredRole(String token, List<String> allowedRoles) {
        return allowedRoles.contains(extractRole(token));
    }
}
