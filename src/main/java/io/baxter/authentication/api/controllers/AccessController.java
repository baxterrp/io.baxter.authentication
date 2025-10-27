package io.baxter.authentication.api.controllers;

import io.baxter.authentication.api.models.LoginRequest;
import io.baxter.authentication.api.models.LoginResponse;
import io.baxter.authentication.api.models.RegistrationRequest;
import io.baxter.authentication.api.models.RegistrationResponse;
import io.baxter.authentication.api.services.AccessService;
import io.baxter.authentication.infrastructure.behavior.exceptions.InvalidLoginException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AccessController {
    private final AccessService accessService;

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request){
        log.info("attempting login for {}", request.getUserName());

        return accessService
                .login(request)
                .map(response -> {
                    log.info("successfully logged in for user {} with token {}", request.getUserName(), response.getToken());
                    return ResponseEntity.ok().body(response);
                })
                .onErrorResume(
                        InvalidLoginException.class,
                        exception -> {
                            log.error("login attempt failed {}", exception.getMessage());
                            return Mono.just(ResponseEntity.status(401).build());
                        });
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<RegistrationResponse>> register(@Valid @RequestBody RegistrationRequest request){
        log.info("attempting registration with username {}", request.getUserName());

        return accessService.register(request)
                .map(response -> {
                    log.info("successfully registered user with username {} and id {}", response.getName(), response.getId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .doOnError(exception -> {
                    log.error("failed registration attempt for username {} with error {}", request.getUserName(), exception.getMessage());
                });
    }
}
