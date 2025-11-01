package io.baxter.authentication.api.controllers;

import io.baxter.authentication.api.models.*;
import io.baxter.authentication.api.services.AccessService;
import io.baxter.authentication.infrastructure.behavior.exceptions.InvalidLoginException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

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
    public Mono<ResponseEntity<Map<String, String>>> register(@Valid @RequestBody RegistrationRequest request){
        log.info("attempting registration with username {}", request.getUserName());

        return accessService.register(request)
                .map(response -> {
                    log.info("successfully registered user with username {} and id {}", response.getName(), response.getId());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(Map.of("id", response.getId().toString(), "name", response.getName()));
                })
                .onErrorResume(exception -> {
                    log.error(exception.getMessage());
                    String message = String.format("failed registration attempt for username %s with error %s", request.getUserName(), exception.getMessage());

                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("Error", message)));
                });
    }
}
