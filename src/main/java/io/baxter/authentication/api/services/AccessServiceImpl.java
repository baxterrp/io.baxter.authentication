package io.baxter.authentication.api.services;

import io.baxter.authentication.api.models.LoginResponse;
import io.baxter.authentication.api.models.RegistrationResponse;
import io.baxter.authentication.data.models.RoleDataModel;
import io.baxter.authentication.data.models.UserRoleDataModel;
import io.baxter.authentication.data.repository.RoleRepository;
import io.baxter.authentication.data.repository.UserRepository;
import io.baxter.authentication.data.repository.UserRoleRepository;
import io.baxter.authentication.infrastructure.behavior.exceptions.InvalidLoginException;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceExistsException;
import io.baxter.authentication.infrastructure.auth.JwtTokenGenerator;
import io.baxter.authentication.infrastructure.auth.PasswordEncryption;
import io.baxter.authentication.api.models.LoginRequest;
import io.baxter.authentication.api.models.RegistrationRequest;
import io.baxter.authentication.data.models.UserDataModel;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
            .switchIfEmpty(Mono.error(new InvalidLoginException()))
            .flatMap(user -> {
                if (!passwordEncryption.verify(request.getPassword(), user.getPassword())){
                    return Mono.error(new InvalidLoginException());
                }

                return userRoleRepository.findByUserId(user.getId())
                    .flatMap(userRole -> roleRepository.findById(userRole.getRoleId()))
                    .map(RoleDataModel::getName)
                    .collectList()
                    .map(roles -> tokenGenerator.generateToken(request.getUserName(), roles))
                    .map(LoginResponse::new);
            });
    }

    // register new user with username and password
    @Override
    public Mono<RegistrationResponse> register(RegistrationRequest request) {
        return userRepository.existsByUsername(request.getUserName())
            .flatMap(exists -> {

                // if username already taken, throw exception (handled by global exception handler to return 409)
                if (exists){
                    return Mono.error(new ResourceExistsException("User", request.getUserName()));
                }

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

                    return userRepository.save(newUser)
                        // result of save is a Mono<user> so flatmap will run against the single result
                        .flatMap(user -> {
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
                                .then(Mono.just(new RegistrationResponse(user.getUsername(), user.getId())));
                        });
                    });
            });
    }
}
