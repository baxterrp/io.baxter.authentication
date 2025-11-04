package io.baxter.authentication.api.services;

import io.baxter.authentication.api.models.*;
import io.baxter.authentication.data.models.RoleDataModel;
import io.baxter.authentication.data.models.UserRoleDataModel;
import io.baxter.authentication.data.repository.RoleRepository;
import io.baxter.authentication.data.repository.UserRepository;
import io.baxter.authentication.data.repository.UserRoleRepository;
import io.baxter.authentication.infrastructure.behavior.exceptions.InvalidLoginException;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceExistsException;
import io.baxter.authentication.infrastructure.auth.JwtTokenGenerator;
import io.baxter.authentication.infrastructure.auth.PasswordEncryption;
import io.baxter.authentication.data.models.UserDataModel;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService{
    private final JwtTokenGenerator tokenGenerator;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncryption passwordEncryption;

    // find existing user by validating username and password, generating jwt token
    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByUsername(request.getUserName())
            .switchIfEmpty(Mono.defer(() -> {
                log.error("account not found for email {}", request.getUserName());
                return Mono.error(new InvalidLoginException());
            }))
            .flatMap(user -> {
                if (!passwordEncryption.verify(request.getPassword(), user.getPassword())){
                    log.error("invalid password used for user {}", request.getUserName());
                    return Mono.error(new InvalidLoginException());
                }

                return userRoleRepository.findByUserId(user.getId())
                    .flatMap(userRole -> {
                        log.info("found user {}, looking up roles", user.getUsername());
                        return roleRepository.findById(userRole.getRoleId());
                    })
                    .map(RoleDataModel::getName)
                    .collectList()
                    .map(roles -> {
                        log.info("found roles {}, generating token", roles);
                        String token = tokenGenerator.generateToken(request.getUserName(), roles);

                        return new LoginResponse(user.getId(), user.getUsername(), user.getUserId(), token);
                    });
            });
    }

    // register new user with username and password
    @Override
    public Mono<RegistrationResponse> register(RegistrationRequest request) {
        log.info("attempting registration for user with username {}", request.getUserName());

        return userRepository.existsByUsername(request.getUserName())
            .flatMap(exists -> {

                // if username already taken, throw exception (handled by global exception handler to return 409)
                if (exists){
                    log.error("user already exists with name {}", request.getUserName());
                    return Mono.error(new ResourceExistsException("User", request.getUserName()));
                }

                log.info("valid user name provided, validating roles");

                // find roles by name and validate they exist
                Mono<List<RoleDataModel>> roleDataModels =  Flux.fromArray(request.getRoles())
                    .flatMap(roleName -> roleRepository.findByName(roleName)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("role", roleName))))

                    // need "collectList" to convert flux<T> to mono<T>
                    // this allows flatmap to map the entire collection as one object instead of iterating each item
                    .collectList();

                // encrypt password and persist new user data model
                // flat map is not iterating roleDataModels, but mapping the entire collection in one function
                return roleDataModels.flatMap(roles -> {
                    String hashedPassword = passwordEncryption.encrypt(request.getPassword());
                    UserDataModel newUser = new UserDataModel(request.getUserName(), hashedPassword);

                    log.info(
                            "saving user {} with roles {}",
                            request.getUserName(),
                            roles.stream().map(RoleDataModel::getName).collect(Collectors.joining(",")));

                    return userRepository.save(newUser)
                        // result of save is a Mono<user> so flatmap will run against the single result
                        .flatMap(user -> {

                            log.info("saved user {}", user.getUsername());
                            List<UserRoleDataModel> userRoles = roles
                                // in order to iterate the collection roles - we must open a "stream"
                                // think of this as the equivalent of LINQ lazy loading IEnumerable items
                                .stream()

                                // map (synchronous) called for each item in collection
                                .map(role -> new UserRoleDataModel(user.getId(), role.getId()))

                                // we don't want to return the stream, this simply converts to List<T>
                                .toList();

                            return Flux.fromIterable(userRoles)
                                // here we are using flatMap on flux
                                // which WILL iterate each userRole and call userRoleRepository.save for each item
                                .flatMap(userRoleRepository::save)

                                // return the registered user with newly generated identity
                                .then(Mono.just(new RegistrationResponse(user.getUsername(), user.getUserId(), user.getId())));
                        });
                    });
            });
    }
}
