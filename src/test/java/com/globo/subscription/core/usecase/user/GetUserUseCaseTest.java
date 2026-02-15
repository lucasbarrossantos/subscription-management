package com.globo.subscription.core.usecase.user;

import com.globo.subscription.core.domain.User;
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
import static org.mockito.Mockito.*;

class GetUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @InjectMocks
    private GetUserUseCase useCase;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@user.com");
        user.setName("Test User");
    }

    @Test
    void execute_shouldReturnUserWhenExists() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        User result = useCase.execute(userId);
        assertThat(result).isEqualTo(user);
        verify(userRepositoryPort).findById(userId);
    }

    @Test
    void execute_shouldThrowWhenUserNotFound() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
        verify(userRepositoryPort).findById(userId);
    }
}
