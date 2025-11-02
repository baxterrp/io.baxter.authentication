package io.baxter.authentication.infrastructure.behavior.handlers;

import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.*;

@Getter
public class AuthServiceErrorResponse {
    private Map<String, String> errors = new HashMap<>();

    // constructor for single exception mapping
    public AuthServiceErrorResponse(Exception exception){
        this.errors = Map.of("message", exception.getMessage());
    }

    // constructor for list of validation errors
    public AuthServiceErrorResponse(List<FieldError> fieldErrors){
        fieldErrors.forEach(error -> this.errors.put(error.getField(), error.getDefaultMessage()));
    }

    // empty default constructor for general errors we don't want to expose to the consumer
    public AuthServiceErrorResponse(){
        this.errors = Map.of("message", "An error occurred on the authentication server");
    }
}
