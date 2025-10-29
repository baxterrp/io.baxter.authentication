package io.baxter.authentication.api.controllers;

import io.baxter.authentication.api.models.UserModel;
import io.baxter.authentication.api.services.UserService;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @GetMapping("{id}")
    public Mono<ResponseEntity<UserModel>> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
            .map(user -> {
                log.info("found user with id {} and username {}", id, user.getUserName());
                return ResponseEntity.ok(user);
            });
    }
}
