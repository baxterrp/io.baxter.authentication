package io.baxter.authentication.tests.api.controllers;

import io.baxter.authentication.api.controllers.AccessController;
import io.baxter.authentication.api.models.*;
import io.baxter.authentication.api.services.AccessService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ControllerTest(controllers = AccessController.class)
public class AccessControllerTest {
    private final Integer testUserId = 1;
    private final String testUserName = "test-user";
    private final String testPassword = "Test-password-123-$$";
    private final String registrationMessage = String.format("attempting registration with username %s", testUserName);
    private final String[] testRoles = new String[] { "TEST_USER" };
    private final ArgumentMatcher<RegistrationRequest> registrationRequestMatcher = registrationRequest ->
            registrationRequest.getUserName().equals(testUserName) && registrationRequest.getPassword().equals(testPassword);
    private final ArgumentMatcher<LoginRequest> loginRequestArgumentMatcher = loginRequest ->
            loginRequest.getUserName().equals(testUserName) && loginRequest.getPassword().equals(testPassword);

    private AccessController accessController;

    @MockitoBean
    AccessService mockAccessService;

    @BeforeEach
    void setup() {
        accessController = new AccessController(mockAccessService);
    }

    @Test
    @DisplayName("Login with valid credentials returns a user id and access token")
    void loginShouldReturnLoginResponseWhenLoginSuccessful(CapturedOutput output){
        // Arrange
        final String token = "abc123";
        LoginRequest loginRequest = new LoginRequest(testUserName, testPassword);
        LoginResponse expectedResponse = new LoginResponse(testUserId, testUserName, token);

        Mockito.when(mockAccessService.login(Mockito.argThat(loginRequestArgumentMatcher)))
                .thenReturn(Mono.just(expectedResponse));

        // Act
        Mono<ResponseEntity<LoginResponse>> result = accessController.login(loginRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                    LoginResponse body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getId()).isEqualTo(testUserId);
                    assertThat(body.getUserName()).isEqualTo(testUserName);
                    assertThat(body.getToken()).isEqualTo(token);

                    return true;
                })
                .verifyComplete();

        Mockito.verify(mockAccessService).login(Mockito.argThat(loginRequestArgumentMatcher));
        Mockito.verifyNoMoreInteractions(mockAccessService);

        String logs = output.getOut();
        assertThat(logs).contains(String.format("attempting login for %s", testUserName));
        assertThat(logs).contains(String.format("successfully logged in for user %s with token %s", testUserName, token));
    }

    @Test
    @DisplayName("Logs a success message and returns id and name when AccessService.register completes successfully")
    void shouldReturnIdAndNameWhenRegistrationSuccessful(CapturedOutput output){
        // Arrange
        final RegistrationRequest request = new RegistrationRequest(testUserName, testPassword, testRoles);
        final RegistrationResponse registrationResponse = new RegistrationResponse(testUserName, testUserId);

        Mockito.when(mockAccessService.register(Mockito.argThat(registrationRequestMatcher)))
                .thenReturn(Mono.just(registrationResponse));

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = accessController.register(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                    Map<String, String> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("id")).isEqualTo(testUserId.toString());
                    assertThat(body.get("name")).isEqualTo(testUserName);

                    return true;
                })
                .verifyComplete();

        Mockito.verify(mockAccessService).register(Mockito.argThat(registrationRequestMatcher));
        Mockito.verifyNoMoreInteractions(mockAccessService);

        String logs = output.getOut();
        assertThat(logs).contains(registrationMessage);
        assertThat(logs).contains(String.format("successfully registered user with username %s and id %s", testUserName, testUserId));
    }

    @Test
    @DisplayName("Logs an exception message when AccessService.register throws an exception")
    void shouldLogErrorWhenAccessServiceFails(CapturedOutput output){
        // Arrange
        final String testExceptionMessage = "test message 123";
        final String expectedResponseMessage = String.format("failed registration attempt for username test-user with error %s", testExceptionMessage);
        final RegistrationRequest request = new RegistrationRequest(testUserName, testPassword, testRoles);

        Mockito.when(mockAccessService.register(Mockito.argThat(registrationRequestMatcher)))
                .thenReturn(Mono.error(new RuntimeException(testExceptionMessage)));

        // Act
        Mono<ResponseEntity<Map<String, String>>> result = accessController.register(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

                    Map<String, String> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("Error")).isEqualTo(expectedResponseMessage);

                    return true;
                })
                .verifyComplete();

        Mockito.verify(mockAccessService).register(Mockito.argThat(registrationRequestMatcher));
        Mockito.verifyNoMoreInteractions(mockAccessService);

        String logs = output.getOut();
        assertThat(logs).contains(registrationMessage);
        assertThat(logs).contains(testExceptionMessage);
    }
}
