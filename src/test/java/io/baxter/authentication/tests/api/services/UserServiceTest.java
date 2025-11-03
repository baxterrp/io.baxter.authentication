package io.baxter.authentication.tests.api.services;

import io.baxter.authentication.api.models.UserModel;
import io.baxter.authentication.api.services.*;
import io.baxter.authentication.data.models.UserDataModel;
import io.baxter.authentication.data.repository.UserRepository;
import io.baxter.authentication.infrastructure.behavior.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository mockUserRepository;

    private final Integer id = 1;

    @Test
    @DisplayName("getUserById should return an UserModel when user is found")
    void getUserByIdShouldReturnUserModelWhenUserFound(){
        // Arrange
        String userName = "test-user";
        String password = "Password123$$";
        UserDataModel userDataModel = new UserDataModel(userName, password);
        userDataModel.setId(id);

        Mockito.when(mockUserRepository.findById(id)).thenReturn(Mono.just(userDataModel));

        // Act
        Mono<UserModel> response = userService.getUserById(id);

        // Assert
        StepVerifier.create(response)
                .expectNextMatches(user ->
                        user != null && user.getUserName().equals(userName) && user.getId().equals(id)
                )
                .verifyComplete();

        Mockito.verify(mockUserRepository).findById(id);
        Mockito.verifyNoMoreInteractions(mockUserRepository);
    }

    @Test
    @DisplayName("getUserById should return an error with ResourceNotFoundException when UserRepository.findById returns empty")
    void getUserByIdShouldReturnResourceNotFoundExceptionWhenRepositoryReturnsEmpty(CapturedOutput output){
        // Arrange
        String expectedLogMessage = String.format("no user found with id %s", id);
        String expectedExceptionMessage = String.format("No user found with id %s", id);

        Mockito.when(mockUserRepository.findById(id)).thenReturn(Mono.empty());

        // Act
        Mono<UserModel> response = userService.getUserById(id);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(exception ->
                    exception instanceof ResourceNotFoundException && exception.getMessage().equals(expectedExceptionMessage)
                )
                .verify();

        Mockito.verify(mockUserRepository).findById(id);
        Mockito.verifyNoMoreInteractions(mockUserRepository);

        String logs = output.getOut();
        assertThat(logs).contains(expectedLogMessage);
    }
}
