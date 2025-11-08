package io.baxter.authentication.infrastructure.behavior.redis;

import lombok.*;

import java.util.*;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    String userName;
    List<String> roles;
    Date issuedAt;
    Date expiresAt;
}
