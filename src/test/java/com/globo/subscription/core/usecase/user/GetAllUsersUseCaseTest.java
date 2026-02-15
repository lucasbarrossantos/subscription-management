package com.globo.subscription.core.usecase.user;

import com.globo.subscription.core.domain.PagedResult;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetAllUsersUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @InjectMocks
    private GetAllUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldReturnPagedResultFromRepository() {
        PagedResult<User> pagedResult = new PagedResult<>(List.of(new User()), 0, 10, 1, 1);
        when(userRepositoryPort.findAll(0, 10, "email@test.com", "Test User")).thenReturn(pagedResult);
        PagedResult<User> result = useCase.execute(0, 10, "email@test.com", "Test User");
        assertThat(result).isEqualTo(pagedResult);
        verify(userRepositoryPort).findAll(0, 10, "email@test.com", "Test User");
    }

    @Test
    void execute_shouldReturnEmptyPagedResultWhenNoUsers() {
        PagedResult<User> pagedResult = new PagedResult<>(List.of(), 0, 10, 0, 0);
        when(userRepositoryPort.findAll(0, 10, null, null)).thenReturn(pagedResult);
        PagedResult<User> result = useCase.execute(0, 10, null, null);
        assertThat(result).isEqualTo(pagedResult);
        verify(userRepositoryPort).findAll(0, 10, null, null);
    }
}
