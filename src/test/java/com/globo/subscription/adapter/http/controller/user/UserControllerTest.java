package com.globo.subscription.adapter.http.controller.user;

import com.globo.subscription.adapter.http.dto.PagedResponse;
import com.globo.subscription.adapter.http.dto.user.UserRequest;
import com.globo.subscription.adapter.http.dto.user.UserResponse;
import com.globo.subscription.adapter.http.dto.user.UserUpdateRequest;
import com.globo.subscription.adapter.http.mapper.UserDTOMapper;
import com.globo.subscription.core.domain.PagedResult;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.port.in.user.CreateUserPort;
import com.globo.subscription.core.port.in.user.DeleteUserPort;
import com.globo.subscription.core.port.in.user.GetAllUsersPort;
import com.globo.subscription.core.port.in.user.GetUserPort;
import com.globo.subscription.core.port.in.user.UpdateUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private CreateUserPort createUserPort;
    @Mock
    private GetAllUsersPort getAllUsersPort;
    @Mock
    private UpdateUserPort updateUserPort;
    @Mock
    private GetUserPort getUserPort;
    @Mock
    private DeleteUserPort deleteUserPort;
    @Mock
    private UserDTOMapper userDTOMapper;
    @InjectMocks
    private UserController controller;

    private User user;
    private UserRequest userRequest;
    private UserUpdateRequest userUpdateRequest;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        user = mock(User.class);
        userRequest = mock(UserRequest.class);
        userUpdateRequest = mock(UserUpdateRequest.class);
        userResponse = mock(UserResponse.class);
    }

    @Test
    void getAll_shouldReturnPagedResponse() {
        List<User> users = List.of(user);
        List<UserResponse> responses = List.of(userResponse);
        PagedResult<User> pagedResult = new PagedResult<>(users, 0, 10, 1, 1);
        when(getAllUsersPort.execute(0, 10, null, null)).thenReturn(pagedResult);
        when(userDTOMapper.toResponse(users)).thenReturn(responses);
        ResponseEntity<PagedResponse<UserResponse>> result = controller.getAll(0, 10, null, null);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().content()).containsExactlyElementsOf(responses);
        assertThat(result.getBody().page()).isZero();
        assertThat(result.getBody().size()).isEqualTo(10);
        assertThat(result.getBody().totalElements()).isEqualTo(1);
        assertThat(result.getBody().totalPages()).isEqualTo(1);
    }

    @Test
    void create_shouldReturnCreatedUser() {
        when(userDTOMapper.toDomain(userRequest)).thenReturn(user);
        when(createUserPort.execute(user)).thenReturn(user);
        when(userDTOMapper.toResponse(user)).thenReturn(userResponse);
        ResponseEntity<UserResponse> result = controller.create(userRequest);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(userResponse);
    }

    @Test
    void update_shouldReturnUpdatedUser() {
        when(userDTOMapper.toDomain(userUpdateRequest)).thenReturn(user);
        when(updateUserPort.execute(userId, user)).thenReturn(user);
        when(userDTOMapper.toResponse(user)).thenReturn(userResponse);
        ResponseEntity<UserResponse> result = controller.update(userId, userUpdateRequest);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(userResponse);
    }

    @Test
    void findById_shouldReturnUser() {
        when(getUserPort.execute(userId)).thenReturn(user);
        when(userDTOMapper.toResponse(user)).thenReturn(userResponse);
        ResponseEntity<UserResponse> result = controller.findById(userId);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(userResponse);
    }

    @Test
    void delete_shouldReturnNoContent() {
        doNothing().when(deleteUserPort).execute(userId);
        ResponseEntity<Void> result = controller.delete(userId);
        assertThat(result.getStatusCode().value()).isEqualTo(204);
        verify(deleteUserPort).execute(userId);
    }
}
