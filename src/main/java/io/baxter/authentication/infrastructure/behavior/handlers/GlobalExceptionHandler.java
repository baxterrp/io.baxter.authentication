package io.baxter.authentication.infrastructure.behavior.handlers;

import io.baxter.authentication.infrastructure.behavior.exceptions.InvalidLoginException;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceExistsException;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidLoginException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidLoginException(InvalidLoginException exception)
    {
        return Mono.just(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.create(exception, HttpStatus.UNAUTHORIZED, exception.getMessage())));
    }

    @ExceptionHandler(ResourceExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResourceExistsException(ResourceExistsException exception)
    {
        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.create(exception, HttpStatus.CONFLICT, exception.getMessage())));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserNotFound(ResourceNotFoundException exception) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.create(exception, HttpStatus.NOT_FOUND, exception.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleInputValidationError(MethodArgumentNotValidException exception){
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneralException(Exception exception) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)

                // introduce logging and store the message
                .body(ErrorResponse.create(exception, HttpStatus.INTERNAL_SERVER_ERROR, "Uh oh.. something went wrong")));
    }
}
