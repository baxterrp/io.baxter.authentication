package io.baxter.authentication.api.models;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {
    @Size(max = 100, message = "username cannot exceed 100 characters")
    @NotEmpty(message = "username is required")
    String userName;

    @Pattern(
            regexp = "^(?=.{8,}$)(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S+$",
            message = "invalid password (at least 8 characters, 1 upper case, 1 lower case, 1 special character)")
    String password;

    @NotEmpty(message = "user must have access roles")
    String[] roles;

    public RegistrationRequest(String userName, String password, String[] roles){
        this.userName = userName;
        this.password = password;
        this.roles = roles;
    }

    public String getUserName(){ return this.userName; }
    public String getPassword(){ return this.password; }
    public String[] getRoles(){ return this.roles; }
}
