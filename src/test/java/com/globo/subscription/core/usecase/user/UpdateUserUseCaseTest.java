package com.globo.subscription.core.usecase.user;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.exception.EmailAlreadyExistsException;
import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @InjectMocks
    private UpdateUserUseCase useCase;

    private UUID userId;
    private User existingUser;
    private User updateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@email.com");
        existingUser.setName("Old Name");
        updateRequest = new User();
    }

    @Test
    void execute_shouldUpdateEmailAndName() {
        updateRequest.setEmail("new@email.com");
        updateRequest.setName("New Name");
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail("new@email.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User result = useCase.execute(userId, updateRequest);
        assertThat(result.getEmail()).isEqualTo("new@email.com");
        assertThat(result.getName()).isEqualTo("New Name");
        verify(userRepositoryPort).save(existingUser);
    }

    @Test
    void execute_shouldUpdateOnlyNameIfEmailIsBlank() {
        updateRequest.setEmail("");
        updateRequest.setName("New Name");
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User result = useCase.execute(userId, updateRequest);
        assertThat(result.getEmail()).isEqualTo("old@email.com");
        assertThat(result.getName()).isEqualTo("New Name");
        verify(userRepositoryPort).save(existingUser);
    }

    @Test
    void execute_shouldUpdateOnlyEmailIfNameIsBlank() {
        updateRequest.setEmail("new@email.com");
        updateRequest.setName("");
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail("new@email.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User result = useCase.execute(userId, updateRequest);
        assertThat(result.getEmail()).isEqualTo("new@email.com");
        assertThat(result.getName()).isEqualTo("Old Name");
        verify(userRepositoryPort).save(existingUser);
    }

    @Test
    void execute_shouldThrowIfUserNotFound() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(userId, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void execute_shouldThrowIfEmailAlreadyExists() {
        updateRequest.setEmail("existing@email.com");
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail("existing@email.com")).thenReturn(true);
        assertThatThrownBy(() -> useCase.execute(userId, updateRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@email.com");
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void execute_shouldNotUpdateIfAllFieldsBlank() {
        updateRequest.setEmail("");
        updateRequest.setName("");
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User result = useCase.execute(userId, updateRequest);
        assertThat(result.getEmail()).isEqualTo("old@email.com");
        assertThat(result.getName()).isEqualTo("Old Name");
        verify(userRepositoryPort).save(existingUser);
    }
}
