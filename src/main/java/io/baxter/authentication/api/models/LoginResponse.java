package io.baxter.authentication.api.models;

public class LoginResponse {
    String token;

    public LoginResponse(String token){
        this.token = token;
    }

    public String getToken() { return this.token; }
}
