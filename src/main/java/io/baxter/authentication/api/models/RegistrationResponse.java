package io.baxter.authentication.api.models;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegistrationResponse {
    String userName;
    UUID userId;
    Integer id;
}
