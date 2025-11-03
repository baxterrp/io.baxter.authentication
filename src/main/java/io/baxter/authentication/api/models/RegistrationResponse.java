package io.baxter.authentication.api.models;

import lombok.*;

@Getter
@AllArgsConstructor
public class RegistrationResponse {
    String userName;
    Integer id;
}
