package io.baxter.authentication.tests.api.services;

import io.baxter.authentication.api.models.*;
import io.baxter.authentication.api.services.AccessServiceImpl;
import io.baxter.authentication.data.models.*;
import io.baxter.authentication.data.repository.*;
import io.baxter.authentication.infrastructure.auth.*;
import io.baxter.authentication.infrastructure.behavior.exceptions.*;
import io.baxter.authentication.infrastructure.behavior.redis.RefreshToken;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.*;
import org.springframework.data.redis.core.*;
import reactor.core.publisher.*;
import reactor.test.StepVerifier;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class AccessServiceTest {
    @Mock private UserRepository mockUserRepository;
    @Mock private UserRoleRepository mockUserRoleRepository;
    @Mock private RoleRepository mockRoleRepository;
    @Mock private PasswordEncryption mockPasswordEncryption;
    @Mock private JwtTokenGenerator mockTokenGenerator;
    @Mock private ReactiveRedisTemplate<String, RefreshToken> mockRedisCache;
    @Mock private Clock clock;
    @Mock ReactiveValueOperations<String, RefreshToken> mockValueOps;

    @InjectMocks private AccessServiceImpl accessService;

    private final String refreshToken = "872bab23-6d67-4946-9144-07ecf0550134";
    private final String refreshTokenWithKey = String.format("refresh_token:%s", refreshToken);
    private final String testUserName = "test-user";
    private final String testPassword = "TestPassword123$$";
    private final Integer userId = 1;
    private final String registerLogMessage = String.format("attempting registration for user with username %s", testUserName);
    private final String validatingRolesLogMessage = "valid user name provided, validating roles";
    private final List<String> validRoles = List.of("TEST_ROLE_1", "TEST_ROLE_2");
    private final List<RoleDataModel> roleDataModels = List.of(
            new RoleDataModel(1, validRoles.getFirst()),
            new RoleDataModel(2, validRoles.get(1)));

    private final List<UserRoleDataModel> userRoleDataModels = List.of(
            new UserRoleDataModel(userId, roleDataModels.getFirst().getId()),
            new UserRoleDataModel(userId, roleDataModels.get(1).getId()));

    private final ArgumentMatcher<String> roleArgumentMatcher = role ->
            validRoles.stream().anyMatch(validRole -> validRole.equals(role));

    @Test
    @DisplayName("refreshAccessToken should return an new valid access token when refresh token exists and is not expired")
    void refreshAccessTokenShouldReturnNewRefreshTokenAndAccessTokenWhenSuccessful(){
        // Arrange
        var accessToken = "22e73eb1-6f8a-4f43-a5dd-24b651b6f51d";
        var frozen = Instant.parse("2025-11-08T12:00:00Z");
        var expiration = Instant.parse("2026-11-08T12:00:00Z");
        var tokenDate = Date.from(expiration);
        var refreshTokenResponse = new RefreshToken(testUserName, validRoles, tokenDate, tokenDate);

        Mockito.when(clock.instant()).thenReturn(frozen);
        Mockito.when(mockRedisCache.opsForValue()).thenReturn(mockValueOps);
        Mockito.when(mockValueOps.get(refreshTokenWithKey)).thenReturn(Mono.just(refreshTokenResponse));
        Mockito.when(mockTokenGenerator.generateToken(testUserName, validRoles)).thenReturn(accessToken);
        Mockito.when(mockValueOps.delete(refreshTokenWithKey)).thenReturn(Mono.just(true));
        Mockito.when(mockValueOps
                        .set(
                                Mockito.argThat(key -> key.startsWith("refresh_token:")),
                                Mockito.argThat(token -> token.getUserName().equals(testUserName))))
                        .thenReturn(Mono.just(true));

        // Act
        var response = accessService.refreshAccessToken(refreshToken);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(token ->
                        token.getAccessToken().equals(accessToken) &&
                        !token.getRefreshToken().isEmpty() &&
                        !token.getRefreshToken().equals(refreshToken))
                .verifyComplete()   ;

        Mockito.verify(clock).instant();
        Mockito.verify(mockValueOps).get(refreshTokenWithKey);
        Mockito.verify(mockValueOps).delete(refreshTokenWithKey);
        Mockito.verify(mockValueOps).set(
                Mockito.argThat((String key) -> key.startsWith("refresh_token:")),
                Mockito.argThat((RefreshToken token) -> token.getUserName().equals(testUserName)));
        Mockito.verifyNoMoreInteractions(mockValueOps);
    }

    @Test
    @DisplayName("refreshAccessToken should return an invalid login exception if the provided refresh token is expired")
    void refreshAccessTokenShouldReturnInvalidLoginExceptionWhenTokenIsExpired(){
        // Arrange
        var frozen = Instant.parse("2025-11-08T12:00:00Z");
        var expiration = Instant.parse("2024-11-08T12:00:00Z");
        var tokenDate = Date.from(expiration);
        var refreshTokenResponse = new RefreshToken(testUserName, validRoles, tokenDate, tokenDate);

        Mockito.when(clock.instant()).thenReturn(frozen);
        Mockito.when(mockRedisCache.opsForValue()).thenReturn(mockValueOps);
        Mockito.when(mockValueOps.get(refreshTokenWithKey)).thenReturn(Mono.just(refreshTokenResponse));
        Mockito.when(mockValueOps.delete(refreshTokenWithKey)).thenReturn(Mono.just(true));

        // Act
        var response = accessService.refreshAccessToken(refreshToken);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(InvalidLoginException.class::isInstance)
                .verify();

        Mockito.verify(clock).instant();
        Mockito.verify(mockValueOps).get(refreshTokenWithKey);
        Mockito.verify(mockValueOps).delete(refreshTokenWithKey);
        Mockito.verifyNoMoreInteractions(mockRedisCache);
        Mockito.verifyNoMoreInteractions(mockValueOps);
    }

    @Test
    @DisplayName("refreshAccessToken when no token cached returns an invalid login exception")
    void refreshAccessTokenShouldReturnInvalidLoginExceptionWhenNoCachedTokenFound(){
        // Arrange
        Mockito.when(mockRedisCache.opsForValue()).thenReturn(mockValueOps);
        Mockito.when(mockValueOps.get(refreshTokenWithKey)).thenReturn(Mono.empty());

        // Act
        var response = accessService.refreshAccessToken(refreshToken);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(InvalidLoginException.class::isInstance)
                .verify();
    }

    @Test
    @DisplayName("if no user is found when logging in an InvalidLoginException should be returned")
    void loginShouldReturnInvalidLoginExceptionWhenNoUserFound(CapturedOutput output){
        // Arrange
        String expectedLogMessage = String.format("account not found for email %s", testUserName);
        LoginRequest request = new LoginRequest(testUserName, testPassword);

        Mockito.when(mockUserRepository.findByUsername(testUserName)).thenReturn(Mono.empty());

        // Act
        var response = accessService.login(request);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(InvalidLoginException.class::isInstance)
                .verify();

        Mockito.verify(mockUserRepository).findByUsername(testUserName);
        Mockito.verifyNoMoreInteractions(mockUserRepository);

        String logs = output.getOut();
        assertThat(logs).contains(expectedLogMessage);
    }

    @Test
    @DisplayName("if invalid password provided when logging in an InvalidLoginException should be returned")
    void loginShouldReturnInvalidLoginExceptionWhenInvalidPasswordProvided(CapturedOutput output){
        // Arrange
        var expectedLogMessage = String.format("invalid password used for user %s", testUserName);
        var invalidPassword = "invalid-password";

        // invalid password doesn't actually trigger false response - just doing this for readability
        var request = new LoginRequest(testUserName, invalidPassword);
        var userDataModel = new UserDataModel(testUserName, testPassword);
        userDataModel.setId(userId);

        Mockito.when(mockUserRepository.findByUsername(testUserName)).thenReturn(Mono.just(userDataModel));
        Mockito.when(mockPasswordEncryption.verify(invalidPassword, testPassword)).thenReturn(false);

        // Act
        var response = accessService.login(request);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(InvalidLoginException.class::isInstance)
                .verify();

        Mockito.verify(mockUserRepository).findByUsername(testUserName);
        Mockito.verify(mockPasswordEncryption).verify(invalidPassword, testPassword);
        Mockito.verifyNoMoreInteractions(mockUserRepository);

        String logs = output.getOut();
        assertThat(logs).contains(expectedLogMessage);
    }

    @Test
    @DisplayName("if valid username and password provided, roles should be looked up and jwt token generated")
    void loginShouldReturnLoginResponseWhenValidLoginRequestProvided(CapturedOutput output){
        // Arrange
        var roleString = String.join(", ", validRoles);
        var lookingUpRolesLogMessage = String.format("found user %s, looking up roles", testUserName);
        var foundRolesLogMessage = String.format("found roles [%s], generating token", roleString);
        var token = "abc123";

        var request = new LoginRequest(testUserName, testPassword);
        var userDataModel = new UserDataModel(testUserName, testPassword);
        userDataModel.setId(userId);

        Mockito.when(mockUserRepository.findByUsername(testUserName)).thenReturn(Mono.just(userDataModel));
        Mockito.when(mockPasswordEncryption.verify(testPassword, testPassword)).thenReturn(true);
        Mockito.when(mockUserRoleRepository.findByUserId(userId)).thenReturn(Flux.fromIterable(userRoleDataModels));
        Mockito.when(mockRoleRepository.findById(1)).thenReturn(Mono.just(roleDataModels.getFirst()));
        Mockito.when(mockRoleRepository.findById(2)).thenReturn(Mono.just(roleDataModels.get(1)));
        Mockito.when(mockTokenGenerator.generateToken(testUserName, validRoles)).thenReturn(token);
        Mockito.when(mockRedisCache.opsForValue()).thenReturn(mockValueOps);
        Mockito.when(mockValueOps.set(Mockito.anyString(), Mockito.any())).thenReturn(Mono.just(true));

        // Act
        var response = accessService.login(request);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(loginResponse ->
                        loginResponse.getUserName().equals(testUserName) && loginResponse.getId() == userId)
                .verifyComplete();

        Mockito.verify(mockUserRepository).findByUsername(testUserName);
        Mockito.verify(mockPasswordEncryption).verify(testPassword, testPassword);
        Mockito.verify(mockUserRoleRepository).findByUserId(userId);
        Mockito.verify(mockRoleRepository).findById(1);
        Mockito.verify(mockRoleRepository).findById(2);
        Mockito.verify(mockTokenGenerator).generateToken(testUserName, validRoles);
        Mockito.verify(mockRedisCache).opsForValue();
        BDDMockito.verify(mockValueOps).set(Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(mockUserRepository);

        String logs = output.getOut();
        assertThat(logs)
                .contains(lookingUpRolesLogMessage)
                .contains(foundRolesLogMessage);
    }

    @Test
    @DisplayName("on register, user name is already used a ResourceExistsException should be returned")
    void registerShouldReturnResourceExistsExceptionWhenUserNameFound(CapturedOutput output){
        // Arrange
        var expectedExceptionMessage = String.format("User already exists with value %s", testUserName);
        var expectedLogErrorMessage = String.format("user already exists with name %s", testUserName);
        var request = new RegistrationRequest(testUserName, testPassword, validRoles.toArray(String[]::new));

        Mockito.when(mockUserRepository.existsByUsername(testUserName)).thenReturn(Mono.just(true));

        // Act
        var response = accessService.register(request);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(exception ->
                        exception instanceof ResourceExistsException && exception.getMessage().equals(expectedExceptionMessage))
                .verify();

        Mockito.verify(mockUserRepository).existsByUsername(testUserName);
        Mockito.verifyNoMoreInteractions(mockUserRepository);

        String logs = output.getOut();
        assertThat(logs)
                .contains(registerLogMessage)
                .contains(expectedLogErrorMessage);
    }

    @Test
    @DisplayName("on register, if provided roles are not found a ResourceNotFoundException should be returned")
    void registerShouldReturnResourceNotFoundExceptionWhenNoRolesFound(CapturedOutput output){
        var expectedExceptionMessage = String.format("No role found with id %s", validRoles.getFirst());
        var request = new RegistrationRequest(testUserName, testPassword, validRoles.toArray(String[]::new));

        Mockito.when(mockUserRepository.existsByUsername(testUserName)).thenReturn(Mono.just(false));
        Mockito.when(mockRoleRepository.findByName(Mockito.argThat(roleArgumentMatcher))).thenReturn(Mono.empty());

        // Act
        var response = accessService.register(request);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(exception ->
                        exception instanceof ResourceNotFoundException && exception.getMessage().equals(expectedExceptionMessage))
                .verify();

        Mockito.verify(mockUserRepository).existsByUsername(testUserName);
        Mockito.verify(mockRoleRepository).findByName(validRoles.getFirst());
        Mockito.verifyNoMoreInteractions(mockUserRepository);

        var logs = output.getOut();
        assertThat(logs)
                .contains(registerLogMessage)
                .contains(validatingRolesLogMessage);
    }

    @Test
    @DisplayName("on register, valid registration provided with valid roles, a registration response is returned")
    void registerShouldReturnRegistrationResponseWhenValidCredentialsProvided(CapturedOutput output){
        var roleString = String.join(",", validRoles);
        var savingUserLogMessage = String.format("saving user %s with roles %s", testUserName, roleString);
        var savedUserLogMessage = String.format("saved user %s", testUserName);

        var request = new RegistrationRequest(testUserName, testPassword, validRoles.toArray(String[]::new));
        var user = new UserDataModel(testUserName, testPassword);
        user.setId(userId);

        ArgumentMatcher<UserRoleDataModel> roleOneDataModelMatcher = role -> role != null && role.getRoleId().equals(userRoleDataModels.getFirst().getRoleId());
        ArgumentMatcher<UserRoleDataModel> roleTwoDataModelMatcher = role -> role != null && role.getRoleId().equals(userRoleDataModels.get(1).getRoleId());

        Mockito.when(mockUserRepository.existsByUsername(testUserName)).thenReturn(Mono.just(false));
        Mockito.when(mockRoleRepository.findByName(validRoles.getFirst())).thenReturn(Mono.just(roleDataModels.getFirst()));
        Mockito.when(mockRoleRepository.findByName(validRoles.get(1))).thenReturn(Mono.just(roleDataModels.get(1)));
        Mockito.when(mockUserRepository.save(Mockito.argThat(savedUser -> savedUser.getUsername().equals(testUserName)))).thenReturn(Mono.just(user));
        Mockito.when(mockUserRoleRepository.save(Mockito.argThat(roleOneDataModelMatcher))).thenReturn(Mono.empty());
        Mockito.when(mockUserRoleRepository.save(Mockito.argThat(roleTwoDataModelMatcher))).thenReturn(Mono.empty());

        // Act
        var response = accessService.register(request);

        // Assert
        StepVerifier.create(response).expectNextMatches(registration ->
                        registration.getUserName().equals(testUserName) && registration.getId().equals(userId))
                .verifyComplete();

        Mockito.verify(mockUserRepository).existsByUsername(testUserName);
        Mockito.verify(mockRoleRepository).findByName(validRoles.getFirst());
        Mockito.verify(mockRoleRepository).findByName(validRoles.get(1));
        Mockito.verify(mockUserRepository).save(Mockito.argThat(savedUser -> savedUser.getUsername().equals(testUserName)));
        Mockito.verify(mockUserRoleRepository).save(Mockito.argThat(role -> role.getRoleId().equals(userRoleDataModels.getFirst().getRoleId())));
        Mockito.verify(mockUserRoleRepository).save(Mockito.argThat(role -> role.getRoleId().equals(userRoleDataModels.get(1).getRoleId())));
        Mockito.verifyNoMoreInteractions(mockUserRepository);

        var logs = output.getOut();
        assertThat(logs)
                .contains(registerLogMessage)
                .contains(validatingRolesLogMessage)
                .contains(savingUserLogMessage)
                .contains(savedUserLogMessage);
    }
}
