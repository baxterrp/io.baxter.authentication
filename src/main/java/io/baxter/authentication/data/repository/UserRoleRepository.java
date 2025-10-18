package io.baxter.authentication.data.repository;

import io.baxter.authentication.data.models.UserRoleDataModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRoleDataModel, Void> {
    Flux<UserRoleDataModel> findByUserId(Integer userId);
}
