package com.globo.subscription.core.usecase.user;

import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DeleteUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @InjectMocks
    private DeleteUserUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
    }

    @Test
    void execute_shouldDeleteUserWhenExists() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(new com.globo.subscription.core.domain.User()));
        doNothing().when(userRepositoryPort).delete(userId);
        useCase.execute(userId);
        verify(userRepositoryPort).delete(userId);
    }

    @Test
    void execute_shouldThrowWhenUserNotFound() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
        verify(userRepositoryPort, never()).delete(any());
    }
}
