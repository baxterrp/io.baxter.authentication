package io.baxter.authentication.tests.api.controllers;

import io.baxter.authentication.api.controllers.UserController;
import io.baxter.authentication.api.models.UserModel;
import io.baxter.authentication.api.services.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ControllerTest(controllers = UserController.class)
public class UserControllerTest {
    private final Integer testUserId = 1;

    private UserController userController;

    @MockitoBean
    UserService mockUserService;

    @BeforeEach
    void setup() {
        userController = new UserController(mockUserService);
    }

    @Test
    @DisplayName("getUserById when exception thrown, logs error")
    void getUserByIdShouldLogException(CapturedOutput output){
        // Arrange
        final String exceptionMessage = "Test error 123";
        final String expectedErrorMessage =
                String.format("unable to fetch user with id %s with error %s", testUserId, exceptionMessage);

        Mockito.when(mockUserService.getUserById(testUserId))
                .thenReturn(Mono.error(new RuntimeException(exceptionMessage)));

        // Act
        Mono<ResponseEntity<UserModel>> response = userController.getUserById(testUserId);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(exception ->
                        exception instanceof RuntimeException && exception.getMessage().equals(exceptionMessage)
                )
                .verify();

        Mockito.verify(mockUserService).getUserById(1);
        Mockito.verifyNoMoreInteractions(mockUserService);

        String logs = output.getOut();
        assertThat(logs).contains(expectedErrorMessage);
    }

    @Test
    @DisplayName("getUserById when user is found, returns user")
    void getUserByIdShouldReturnUserModelWhenFound(CapturedOutput output){
        // Arrange
        final String testUserName = "test-user";
        final UserModel expectedUser = new UserModel(testUserId, testUserName);
        final String expectedLogMessage =
                String.format("found user with id %s and username %s", testUserId, testUserName);

        Mockito.when(mockUserService.getUserById(testUserId)).thenReturn(Mono.just(expectedUser));

        // Act
        Mono<ResponseEntity<UserModel>> response = userController.getUserById(testUserId);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(userResult -> {
                    UserModel user = userResult.getBody();
                    assertThat(user).isNotNull();

                    return user.getId().equals(testUserId) && user.getUserName().equals(testUserName);
                })
                .verifyComplete();

        Mockito.verify(mockUserService).getUserById(testUserId);
        Mockito.verifyNoMoreInteractions(mockUserService);

        String logs = output.getOut();
        assertThat(logs).contains(expectedLogMessage);
    }
}
