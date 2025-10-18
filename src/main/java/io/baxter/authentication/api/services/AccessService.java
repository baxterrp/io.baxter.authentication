package io.baxter.authentication.api.services;

import io.baxter.authentication.api.models.LoginRequest;
import io.baxter.authentication.api.models.LoginResponse;
import io.baxter.authentication.api.models.RegistrationRequest;
import io.baxter.authentication.api.models.RegistrationResponse;
import reactor.core.publisher.Mono;

public interface AccessService {
    Mono<LoginResponse> login(LoginRequest request);
    Mono<RegistrationResponse> register(RegistrationRequest request);
}
