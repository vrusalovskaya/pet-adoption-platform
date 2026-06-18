package com.wise.petadoption.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserEntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public User create(CreateUserCommand command) {
        UserEntity userEntity = entityMapper.toEntity(command);
        userEntity.setPasswordHash(passwordEncoder.encode(command.rawPassword()));
        UserEntity saved = userRepository.save(userEntity);
        entityManager.flush();
        entityManager.refresh(saved);
        return entityMapper.toModel(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(entityMapper::toModel);
    }
}
