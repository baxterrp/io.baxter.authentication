package io.baxter.authentication.tests.api.controllers;

import io.baxter.authentication.api.controllers.AccessController;
import io.baxter.authentication.api.models.*;
import io.baxter.authentication.api.services.AccessService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@ControllerTest(controllers = AccessController.class)
public class AccessControllerTest {
    private final String testExceptionMessage = "test message 123";
    private final String testUser = "test-user";
    private final String testPassword = "Test-password-123-$$";
    private final String[] testRoles = new String[] { "TEST_USER" };
    private final String registrationMessage = String.format("attempting registration with username %s", testUser);

    @Autowired
    WebTestClient testClient;

    @MockitoBean
    AccessService mockAccessService;

    @Test
    @DisplayName("Logs a success message and returns id and name when AccessService.register completes successfully")
    void shouldReturnIdAndNameWhenRegistrationSuccessful(CapturedOutput output){
        // Arrange
        final Integer userId = 1;
        final RegistrationRequest request = new RegistrationRequest(testUser, testPassword, testRoles);
        final RegistrationResponse registrationResponse = new RegistrationResponse(testUser, userId);

        Mockito.when(mockAccessService.register(Mockito.any())).thenReturn(Mono.just(registrationResponse));

        // Act
        WebTestClient.ResponseSpec response = testClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .exchange();

        // Assert
        response.expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(userId)
                .jsonPath("$.name")
                .isEqualTo(testUser);

        assertThat(output).contains(registrationMessage);
        assertThat(output).contains(String.format("successfully registered user with username %s and id %s", testUser, userId));
    }

    @Test
    @DisplayName("Logs an exception message when AccessService.register throws an exception")
    void shouldLogErrorWhenAccessServiceFails(CapturedOutput output){
        // Arrange
        final String expectedResponseMessage = "failed registration attempt for username test-user with error test message 123";
        final RegistrationRequest request = new RegistrationRequest(testUser, testPassword, testRoles);

        Mockito.when(mockAccessService.register(Mockito.any()))
                .thenReturn(Mono.error(new RuntimeException(testExceptionMessage)));

        // Act
        WebTestClient.ResponseSpec response = testClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .exchange();

        // Assert
        response.expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.Error")
                .isEqualTo(expectedResponseMessage);

        assertThat(output).contains(registrationMessage);
        assertThat(output).contains(testExceptionMessage);
    }
}
