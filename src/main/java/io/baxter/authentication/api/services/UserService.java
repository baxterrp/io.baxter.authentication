package io.baxter.authentication.api.services;

import io.baxter.authentication.api.models.UserModel;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserModel> getUserById(Integer id);
}
