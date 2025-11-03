package io.baxter.authentication.api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@AllArgsConstructor
public class LoginRequest {
    @Email(message = "invalid email format")
    @Size(max = 100, message = "username cannot exceed 100 characters")
    @NotEmpty(message = "username is required")
    @Schema(description = "User Email", example = "robert@test.com")
    String userName;

    @Schema(description = "User Password", example = "b^Rb!?&:nUP5)kT-Bo'oJ9MiJU!^g-Cvz~{[")
    @Pattern(
            regexp = "^(?=.{8,}$)(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S+$",
            message = "invalid password (at least 8 characters, 1 upper case, 1 lower case, 1 special character)")
    String password;
}
