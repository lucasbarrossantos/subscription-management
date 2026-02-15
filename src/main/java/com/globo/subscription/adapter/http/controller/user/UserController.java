package com.globo.subscription.adapter.http.controller.user;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.globo.subscription.adapter.http.controller.user.spec.UserControllerSpec;

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

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController implements UserControllerSpec {

    private final CreateUserPort createUserPort;
    private final GetAllUsersPort getAllUsersPort;
    private final UpdateUserPort updateUserPort;
    private final GetUserPort getUserPort;
    private final DeleteUserPort deleteUserPort;
    private final UserDTOMapper userDTOMapper;

    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name) {
        
        log.info("Request to get all users with page {} and size {} and email {} and name {}", page, size, email, name);
        PagedResult<User> result = getAllUsersPort.execute(page, size, email, name);
        
        PagedResponse<UserResponse> response = new PagedResponse<>(
            userDTOMapper.toResponse(result.content()),
            result.page(),
            result.size(),
            result.totalElements(),
            result.totalPages()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest userRequest) {
        User user = userDTOMapper.toDomain(userRequest);
        User createdUser = createUserPort.execute(user);
        return ResponseEntity.ok(userDTOMapper.toResponse(createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest userRequest) {
        User user = userDTOMapper.toDomain(userRequest);
        User updatedUser = updateUserPort.execute(id, user);
        return ResponseEntity.ok(userDTOMapper.toResponse(updatedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        User user = getUserPort.execute(id);
        return ResponseEntity.ok(userDTOMapper.toResponse(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUserPort.execute(id);
        return ResponseEntity.noContent().build();
    }
}