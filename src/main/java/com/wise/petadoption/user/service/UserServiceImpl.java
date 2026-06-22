package com.wise.petadoption.user.service;

import com.wise.petadoption.user.domain.ChangePasswordCommand;
import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.domain.UpdateProfileCommand;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.exception.EmailAlreadyExistsException;
import com.wise.petadoption.user.exception.InvalidPasswordException;
import com.wise.petadoption.user.exception.UserNotFoundException;
import com.wise.petadoption.user.mapper.UserEntityMapper;
import com.wise.petadoption.user.pesistence.UserEntity;
import com.wise.petadoption.user.pesistence.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserEntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public User create(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        UserEntity userEntity = entityMapper.toEntity(command);
        userEntity.setPasswordHash(passwordEncoder.encode(command.rawPassword()));
        UserEntity saved = userRepository.save(userEntity);
        entityManager.flush();
        entityManager.refresh(saved);
        return entityMapper.toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return entityMapper.toModel(getEntityByEmail(email));
    }

    @Override
    public User findById(Long id) {
        return entityMapper.toModel(getEntityById(id));
    }

    @Override
    @Transactional
    public User updateProfile(Long id, UpdateProfileCommand command) {
        UserEntity userEntity = getEntityById(id);

        if (!userEntity.getEmail().equals(command.email())
            && userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        userEntity.setEmail(command.email());
        userEntity.setFirstName(command.firstName());
        userEntity.setLastName(command.lastName());
        userEntity.setPhone(command.phone());

        return entityMapper.toModel(userEntity);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordCommand command) {
        UserEntity userEntity = getEntityById(userId);

        if (!passwordEncoder.matches(command.oldPassword(), userEntity.getPasswordHash())) {
            throw new InvalidPasswordException("Old Password Mismatch");
        }

        if (passwordEncoder.matches(command.newPassword(), userEntity.getPasswordHash())) {
            throw new InvalidPasswordException("New password must differ from old password");
        }

        userEntity.setPasswordHash(passwordEncoder.encode(command.newPassword()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserEntity userEntity = getEntityById(id);
        userRepository.delete(userEntity);
    }

    private UserEntity getEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserEntity getEntityByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }
}
