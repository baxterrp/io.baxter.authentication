package io.baxter.authentication.infrastructure.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenGenerator {
    private final String secret;
    private final long expiration;

    // example of injecting configuration values - here used to control token generation
    public JwtTokenGenerator(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expiration){
        this.secret = secret;
        this.expiration = expiration;
    }

    public String generateToken(String userName, List<String> roles){
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userName)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
