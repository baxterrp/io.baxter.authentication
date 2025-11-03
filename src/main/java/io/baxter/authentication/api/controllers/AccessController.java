package io.baxter.authentication.api.controllers;

import io.baxter.authentication.api.models.*;
import io.baxter.authentication.api.services.AccessService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Access", description = "Account registration, login, and token management.")
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
                .doOnError(exception ->
                        log.error(
                                "login attempt failed for user name {} with error {}",
                                request.getUserName(),
                                exception.getMessage()));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<RegistrationResponse>> register(@Valid @RequestBody RegistrationRequest request){
        log.info("attempting registration with username {}", request.getUserName());

        return accessService.register(request)
                .map(response -> {
                    log.info("successfully registered user with username {} and id {}", response.getUserName(), response.getId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .doOnError(exception ->
                        log.error(
                                "failed registration attempt for username {} with error {}",
                                request.getUserName(),
                                exception.getMessage()));
    }
}
