package com.globo.subscription.adapter.datasource.database;

import com.globo.subscription.adapter.datasource.database.entity.UserEntity;
import com.globo.subscription.adapter.datasource.database.repository.user.UserRepository;
import com.globo.subscription.adapter.datasource.mapper.UserMapper;
import com.globo.subscription.core.domain.PagedResult;
import com.globo.subscription.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDatabaseAdapterTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserDatabaseAdapter adapter;

    private UserEntity entity;
    private User domain;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        entity = new UserEntity();
        entity.setId(userId);
        entity.setName("Test User");
        entity.setEmail("test@user.com");
        domain = User.builder().id(userId).name("Test User").email("test@user.com").build();
    }

    @Test
    void save_shouldPersistAndReturnDomain() {

        when(userMapper.toEntity(domain)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDomain(entity)).thenReturn(domain);

        User result = adapter.save(domain);

        assertThat(result).isEqualTo(domain);
        verify(userRepository).save(entity);
    }

    @Test
    void existsByEmail_shouldReturnTrueIfExists() {
        when(userRepository.existsByEmail("test@user.com")).thenReturn(true);
        boolean exists = adapter.existsByEmail("test@user.com");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalseIfNotExists() {
        when(userRepository.existsByEmail("notfound@user.com")).thenReturn(false);
        boolean exists = adapter.existsByEmail("notfound@user.com");
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_shouldReturnPagedResult() {

        List<UserEntity> entities = List.of(entity);
        List<User> domains = List.of(domain);

        Page<UserEntity> page = new PageImpl<>(entities, PageRequest.of(0, 1), 1);

        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(userMapper.toDomain(entities)).thenReturn(domains);

        PagedResult<User> result = adapter.findAll(0, 1, "test@user.com", "Test User");

        assertThat(result.content()).containsExactlyElementsOf(domains);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void findByEmail_shouldReturnDomainIfExists() {
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(entity));
        when(userMapper.toDomain(entity)).thenReturn(domain);
        Optional<User> result = adapter.findByEmail("test@user.com");
        assertThat(result).isPresent().contains(domain);
    }

    @Test
    void findByEmail_shouldReturnEmptyIfNotExists() {
        when(userRepository.findByEmail("notfound@user.com")).thenReturn(Optional.empty());
        Optional<User> result = adapter.findByEmail("notfound@user.com");
        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnDomainIfExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(userMapper.toDomain(entity)).thenReturn(domain);
        Optional<User> result = adapter.findById(userId);
        assertThat(result).isPresent().contains(domain);
    }

    @Test
    void findById_shouldReturnEmptyIfNotExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Optional<User> result = adapter.findById(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        adapter.delete(userId);
        verify(userRepository).deleteById(userId);
    }
}
