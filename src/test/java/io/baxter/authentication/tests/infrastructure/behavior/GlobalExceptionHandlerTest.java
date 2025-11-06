package io.baxter.authentication.tests.infrastructure.behavior;

import io.baxter.authentication.data.models.UserDataModel;
import io.baxter.authentication.infrastructure.behavior.exceptions.*;
import io.baxter.authentication.infrastructure.behavior.handlers.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void invalidLogExceptionShouldReturnUnauthorized(){
        // Arrange
        InvalidLoginException exception = new InvalidLoginException();

        // Act
        Mono<ResponseEntity<AuthServiceErrorResponse>> response = handler.handleInvalidLoginException(exception);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(responseEntity ->
                        validateErrorResponse(responseEntity, HttpStatus.UNAUTHORIZED, "message", "Unauthorized"))
                .verifyComplete();
    }

    @Test
    void resourceExistsExceptionShouldReturnConflict(){
        // Arrange
        ResourceExistsException exception = new ResourceExistsException("user", "1");

        // Act
        Mono<ResponseEntity<AuthServiceErrorResponse>> response = handler.handleResourceExistsException(exception);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(responseEntity ->
                        validateErrorResponse(responseEntity, HttpStatus.CONFLICT, "message", "user already exists with value 1"))
                .verifyComplete();
    }

    @Test
    void resourceNotFoundExceptionShouldReturnNotFound(){
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("user", "1");

        // Act
        Mono<ResponseEntity<AuthServiceErrorResponse>> response = handler.handleResourceNotFound(exception);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(responseEntity ->
                        validateErrorResponse(responseEntity, HttpStatus.NOT_FOUND, "message", "No user found with id 1"))
                .verifyComplete();
    }

    @Test
    void webExchangeBindExceptionExceptionShouldReturnBadRequest(){
        // Arrange
        MethodParameter mockParameter = mock(MethodParameter.class);
        BindingResult bindingResult = new BindException(new UserDataModel(null, null), "UserDataModel");
        bindingResult.rejectValue("username", "NotEmpty", "Username is required");

        WebExchangeBindException exception = new WebExchangeBindException(mockParameter, bindingResult);

        // Act
        Mono<ResponseEntity<AuthServiceErrorResponse>> response = handler.handleWebExchangeBindException(exception);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(responseEntity ->
                        validateErrorResponse(responseEntity, HttpStatus.BAD_REQUEST, "username", "Username is required"))
                .verifyComplete();
    }

    @Test
    void MethodArgumentNotValidExceptionExceptionShouldReturnBadRequest(){
        // Arrange
        MethodParameter mockParameter = mock(MethodParameter.class);
        BindingResult bindingResult = new BindException(new UserDataModel(null, null), "UserDataModel");
        bindingResult.rejectValue("username", "NotEmpty", "Username is required");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(mockParameter, bindingResult);

        // Act
        Mono<ResponseEntity<AuthServiceErrorResponse>> response = handler.handleInputValidationError(exception);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(responseEntity ->
                        validateErrorResponse(responseEntity, HttpStatus.BAD_REQUEST, "username", "Username is required"))
                .verifyComplete();
    }

    @Test
    void generalExceptionShouldReturnInternalServerError(CapturedOutput output){
        // Arrange
        Exception exception = new Exception("Whoops!");

        // Act
        Mono<ResponseEntity<AuthServiceErrorResponse>> response = handler.handleGeneralException(exception);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(responseEntity ->
                        validateErrorResponse(responseEntity, HttpStatus.INTERNAL_SERVER_ERROR, "message", "An error occurred on the authentication server"))
                .verifyComplete();

        String logs = output.getOut();
        assertThat(logs).contains("Whoops!");
    }

    private static boolean validateErrorResponse(ResponseEntity<AuthServiceErrorResponse> response, HttpStatus status, String errorName, String errorMessage){
        AuthServiceErrorResponse errorResponse = response.getBody();

        if (errorResponse == null){
            return false;
        }

        Map<String, String> errors = errorResponse.getErrors();

        if (errors == null || !errors.containsKey(errorName)){
            return false;
        }

        return response.getStatusCode().equals(status) && errors.get(errorName).equals(errorMessage);
    }
}
