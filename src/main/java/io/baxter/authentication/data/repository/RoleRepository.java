package io.baxter.authentication.data.repository;

import io.baxter.authentication.data.models.RoleDataModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<RoleDataModel, Integer> {
    Mono<RoleDataModel> findById(Integer id);
    Mono<RoleDataModel> findByName(String name);
}
