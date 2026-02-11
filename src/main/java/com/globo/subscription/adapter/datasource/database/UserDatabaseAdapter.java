package com.globo.subscription.adapter.datasource.database;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.globo.subscription.adapter.datasource.database.entity.UserEntity;
import com.globo.subscription.adapter.datasource.database.repository.UserRepository;
import com.globo.subscription.adapter.datasource.mapper.UserMapper;
import com.globo.subscription.core.domain.PagedResult;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;

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
    public PagedResult<User> findAll(int page, int size, String email, String name) {
        PageRequest pageRequest = PageRequest.of(page, size);
        
        Specification<UserEntity> spec = Specification.where((root, query, cb) -> cb.conjunction());
        
        if (email != null && !email.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("email"), email));
        }
        
        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        
        Page<UserEntity> pageResult = userRepository.findAll(spec, pageRequest);
        
        return new PagedResult<>(
            userMapper.toDomain(pageResult.getContent()),
            pageResult.getNumber(),
            pageResult.getSize(),
            pageResult.getTotalElements(),
            pageResult.getTotalPages()
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}