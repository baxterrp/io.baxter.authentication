package io.baxter.authentication.tests.api.services;

import io.baxter.authentication.api.models.*;
import io.baxter.authentication.api.services.AccessServiceImpl;
import io.baxter.authentication.data.models.*;
import io.baxter.authentication.data.repository.*;
import io.baxter.authentication.infrastructure.auth.*;
import io.baxter.authentication.infrastructure.behavior.exceptions.InvalidLoginException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.*;
import reactor.core.publisher.*;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class AccessServiceTest {
    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private UserRoleRepository mockUserRoleRepository;

    @Mock
    private RoleRepository mockRoleRepository;

    @Mock
    private PasswordEncryption mockPasswordEncryption;

    @Mock
    private JwtTokenGenerator mockTokenGenerator;

    @InjectMocks
    private AccessServiceImpl accessService;

    private final String testUserName = "test-user";
    private final String testPassword = "TestPassword123$$";
    private final Integer userId = 1;

    @Test
    @DisplayName("if no user is found when logging in an InvalidLoginException should be returned")
    void loginShouldReturnInvalidLoginExceptionWhenNoUserFound(CapturedOutput output){
        // Arrange
        String expectedLogMessage = String.format("account not found for email %s", testUserName);
        String expectedExceptionMessage = "Unauthorized";
        LoginRequest request = new LoginRequest(testUserName, testPassword);

        Mockito.when(mockUserRepository.findByUsername(testUserName)).thenReturn(Mono.empty());

        // Act
        Mono<LoginResponse> response = accessService.login(request);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(exception ->
                        exception instanceof InvalidLoginException && exception.getMessage().equals(expectedExceptionMessage))
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
        String expectedLogMessage = String.format("invalid password used for user %s", testUserName);
        String expectedExceptionMessage = "Unauthorized";
        String invalidPassword = "invalid-password";

        // invalid password doesn't actually trigger false response - just doing this for readability
        LoginRequest request = new LoginRequest(testUserName, invalidPassword);
        UserDataModel userDataModel = new UserDataModel(testUserName, testPassword);
        userDataModel.setId(userId);

        Mockito.when(mockUserRepository.findByUsername(testUserName)).thenReturn(Mono.just(userDataModel));
        Mockito.when(mockPasswordEncryption.verify(invalidPassword, testPassword)).thenReturn(false);

        // Act
        Mono<LoginResponse> response = accessService.login(request);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(exception ->
                        exception instanceof InvalidLoginException && exception.getMessage().equals(expectedExceptionMessage))
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
        List<RoleDataModel> roleDataModels =
                List.of(new RoleDataModel(1, "TEST_ROLE_1"), new RoleDataModel(2, "TEST_ROLE_2"));
        List<UserRoleDataModel> userRoleDataModels =
                List.of(new UserRoleDataModel(userId, 1), new UserRoleDataModel(userId, 2));

        List<String> roleNames = roleDataModels.stream().map(RoleDataModel::getName).toList();
        String roleString = String.join(", ", roleNames);
        String lookingUpRolesLogMessage = String.format("found user %s, looking up roles", testUserName);
        String foundRolesLogMessage = String.format("found roles [%s], generating token", roleString);
        String token = "abc123";

        LoginRequest request = new LoginRequest(testUserName, testPassword);
        UserDataModel userDataModel = new UserDataModel(testUserName, testPassword);
        userDataModel.setId(userId);

        Mockito.when(mockUserRepository.findByUsername(testUserName)).thenReturn(Mono.just(userDataModel));
        Mockito.when(mockPasswordEncryption.verify(testPassword, testPassword)).thenReturn(true);
        Mockito.when(mockUserRoleRepository.findByUserId(userId)).thenReturn(Flux.fromIterable(userRoleDataModels));
        Mockito.when(mockRoleRepository.findById(1)).thenReturn(Mono.just(roleDataModels.getFirst()));
        Mockito.when(mockRoleRepository.findById(2)).thenReturn(Mono.just(roleDataModels.get(1)));
        Mockito.when(mockTokenGenerator.generateToken(testUserName, roleNames)).thenReturn(token);

        // Act
        Mono<LoginResponse> response = accessService.login(request);

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
        Mockito.verify(mockTokenGenerator).generateToken(testUserName, roleNames);
        Mockito.verifyNoMoreInteractions(mockUserRepository);

        String logs = output.getOut();
        assertThat(logs).contains(lookingUpRolesLogMessage);
        assertThat(logs).contains(foundRolesLogMessage);
    }
}
