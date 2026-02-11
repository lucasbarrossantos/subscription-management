package com.globo.subscription.adapter.http.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.globo.subscription.adapter.http.dto.user.UserRequest;
import com.globo.subscription.adapter.http.dto.user.UserResponse;
import com.globo.subscription.adapter.http.mapper.UserDTOMapper;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.port.in.CreateUserPort;
import com.globo.subscription.core.port.in.GetAllUsersPort;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final CreateUserPort createUserPort;
    private final GetAllUsersPort getAllUsersPort;
    private final UserDTOMapper userDTOMapper;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = getAllUsersPort.execute();
        return ResponseEntity.ok(userDTOMapper.toResponse(users));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        User user = userDTOMapper.toDomain(userRequest);
        User createdUser = createUserPort.execute(user);
        return ResponseEntity.ok(userDTOMapper.toResponse(createdUser));
    }
}