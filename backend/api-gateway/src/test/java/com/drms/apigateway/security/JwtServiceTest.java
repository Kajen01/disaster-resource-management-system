package com.drms.apigateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.drms.apigateway.config.RouteSecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "ZHJtcy1zaGFyZWQtc2VjcmV0LWRybXMtc2hhcmVkLXNlY3JldC1kcm1zLXNoYXJlZC1zZWNyZXQ=";

    @Test
    void extractsSubjectRoleAndUserIdFromToken() {
        RouteSecurityProperties properties = new RouteSecurityProperties(SECRET, List.of(), List.of());
        JwtService jwtService = new JwtService(properties);
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

        String token = Jwts.builder()
                .subject("donor@test.com")
                .claim("role", "DONOR")
                .claim("userId", 42L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertEquals("donor@test.com", jwtService.extractSubject(token));
        assertEquals("DONOR", jwtService.extractRole(token));
        assertEquals("42", jwtService.extractUserId(token));
        assertTrue(jwtService.hasRequiredRole(token, List.of("DONOR", "ADMIN")));
    }
}
