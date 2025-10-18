package io.baxter.authentication.data.repository;

import io.baxter.authentication.data.models.UserDataModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserDataModel, Integer> {
    Mono<Boolean> existsByUsername(String username);
    Mono<UserDataModel> findByUsername(String username);
}
