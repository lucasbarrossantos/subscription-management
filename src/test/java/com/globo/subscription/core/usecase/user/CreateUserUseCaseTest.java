package com.globo.subscription.core.usecase.user;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.exception.EmailAlreadyExistsException;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CreateUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @InjectMocks
    private CreateUserUseCase useCase;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@user.com");
        user.setName("Test User");
    }

    @Test
    void execute_shouldCreateUserWhenEmailDoesNotExist() {
        when(userRepositoryPort.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepositoryPort.save(user)).thenReturn(user);
        User result = useCase.execute(user);
        assertThat(result).isEqualTo(user);
        verify(userRepositoryPort).save(user);
    }

    @Test
    void execute_shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepositoryPort.existsByEmail(user.getEmail())).thenReturn(true);
        assertThatThrownBy(() -> useCase.execute(user))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(user.getEmail());
        verify(userRepositoryPort, never()).save(any());
    }
}
