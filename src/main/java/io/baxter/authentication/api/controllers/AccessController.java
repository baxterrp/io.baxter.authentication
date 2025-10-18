package io.baxter.authentication.api.controllers;

import io.baxter.authentication.api.models.LoginRequest;
import io.baxter.authentication.api.models.LoginResponse;
import io.baxter.authentication.api.models.RegistrationRequest;
import io.baxter.authentication.api.models.RegistrationResponse;
import io.baxter.authentication.api.services.AccessService;
import io.baxter.authentication.infrastructure.behavior.exceptions.InvalidLoginException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccessController {
    private final AccessService accessService;

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request){
        return accessService
                .login(request)
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(
                        InvalidLoginException.class,
                        exception ->
                                Mono.just(ResponseEntity.status(401).build()));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<RegistrationResponse>> register(@Valid @RequestBody RegistrationRequest request){
        return accessService.register(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
}
