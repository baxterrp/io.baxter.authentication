package io.baxter.authentication.api.services;

import io.baxter.authentication.api.models.UserModel;
import io.baxter.authentication.data.repository.UserRepository;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    public Mono<UserModel> getUserById(Integer id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("no user found with id {}", id);
                    return Mono.error(new ResourceNotFoundException("user", id.toString()));
                }))
                .map(user -> new UserModel(user.getId(), user.getUsername()));
    }
}
