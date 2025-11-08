package io.baxter.authentication.api.models;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginResponse {
    int id;
    String userName;
    UUID userId;
    String accessToken;
    String refreshToken;
}
