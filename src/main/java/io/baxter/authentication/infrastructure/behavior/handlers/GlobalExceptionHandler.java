package io.baxter.authentication.infrastructure.behavior.handlers;

import io.baxter.authentication.infrastructure.behavior.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidLoginException.class)
    public Mono<ResponseEntity<AuthServiceErrorResponse>> handleInvalidLoginException(InvalidLoginException exception) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthServiceErrorResponse(exception)));
    }

    @ExceptionHandler(ResourceExistsException.class)
    public Mono<ResponseEntity<AuthServiceErrorResponse>> handleResourceExistsException(ResourceExistsException exception)
    {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthServiceErrorResponse(exception)));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<AuthServiceErrorResponse>> handleResourceNotFound(ResourceNotFoundException exception) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new AuthServiceErrorResponse(exception)));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<AuthServiceErrorResponse>> handleWebExchangeBindException(WebExchangeBindException exception) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthServiceErrorResponse(exception.getFieldErrors())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<AuthServiceErrorResponse>> handleInputValidationError(MethodArgumentNotValidException exception){
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthServiceErrorResponse(exception.getFieldErrors())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<AuthServiceErrorResponse>> handleGeneralException(Exception exception) {
        log.error("exception occurred in auth service", exception);

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthServiceErrorResponse()));
    }
}
