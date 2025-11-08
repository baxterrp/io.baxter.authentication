package io.baxter.authentication.api.services;

import io.baxter.authentication.api.models.*;
import io.baxter.authentication.infrastructure.behavior.redis.RefreshTokenResponse;
import reactor.core.publisher.Mono;

public interface AccessService {
    Mono<RefreshTokenResponse> refreshAccessToken(String refreshToken);
    Mono<LoginResponse> login(LoginRequest request);
    Mono<RegistrationResponse> register(RegistrationRequest request);
}
