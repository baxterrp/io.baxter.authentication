package io.baxter.authentication.infrastructure.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

@Slf4j
@Service
@Generated
public class JwtTokenGenerator {
    private final String secret;
    private final long expiration;

    // example of injecting configuration values - here used to control token generation
    public JwtTokenGenerator(
            @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret,
            @Value("${jwt.expiration-ms}") long expiration){
        this.secret = secret;
        this.expiration = expiration;
    }

    public String generateToken(String userName, List<String> roles){
        log.info("generating token using secret" + secret);
        String roleDefinition = String.join(" ", roles);
        Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));

        return Jwts.builder()
                .setSubject(userName)
                .claim("scope", roleDefinition)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
