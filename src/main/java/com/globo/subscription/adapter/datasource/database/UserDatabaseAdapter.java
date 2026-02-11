package com.globo.subscription.adapter.datasource.database;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.globo.subscription.adapter.datasource.database.entity.UserEntity;
import com.globo.subscription.adapter.datasource.database.repository.UserRepository;
import com.globo.subscription.adapter.datasource.mapper.UserMapper;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UserDatabaseAdapter implements UserRepositoryPort {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity userEntity = userMapper.toEntity(user);
        UserEntity savedEntity = userRepository.save(userEntity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userMapper.toDomain(userRepository.findAll());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDomain);
    }
}