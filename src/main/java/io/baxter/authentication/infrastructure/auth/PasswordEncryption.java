package io.baxter.authentication.infrastructure.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncryption {
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encrypt(String password){
        return encoder.encode(password);
    }

    public boolean verify(String password, String encryptedPassword){
        return encoder.matches(password, encryptedPassword);
    }
}
