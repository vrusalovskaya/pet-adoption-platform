package com.wise.petadoption.user.service;

import com.wise.petadoption.user.domain.ChangePasswordCommand;
import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.domain.UpdateProfileCommand;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.exception.EmailAlreadyExistsException;
import com.wise.petadoption.user.exception.InvalidPasswordException;
import com.wise.petadoption.user.exception.UserNotFoundException;
import com.wise.petadoption.user.mapper.UserEntityMapper;
import com.wise.petadoption.user.persistence.UserEntity;
import com.wise.petadoption.user.persistence.UserRepository;
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
        validateEmail(command.email());
        UserEntity userEntity = entityMapper.toEntity(command);
        userEntity.setPasswordHash(passwordEncoder.encode(command.rawPassword()));
        UserEntity saved = saveAndRefresh(userEntity);
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
    public User updateProfile(UpdateProfileCommand command) {
        UserEntity userEntity = getEntityById(command.id());

        if (!userEntity.getEmail().equals(command.email())) {
            validateEmail(command.email());
        }

        entityMapper.updateEntity(command, userEntity);
        return entityMapper.toModel(userEntity);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        UserEntity userEntity = getEntityById(command.id());
        validatePassword(command, userEntity.getPasswordHash());
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

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    private UserEntity saveAndRefresh(UserEntity userEntity) {
        UserEntity saved = userRepository.save(userEntity);
        entityManager.flush();
        entityManager.refresh(saved);
        return saved;
    }

    private void validatePassword(ChangePasswordCommand command, String currentPasswordHash) {
        if (!passwordEncoder.matches(command.oldPassword(), currentPasswordHash)) {
            throw new InvalidPasswordException("Old Password Mismatch");
        }

        if (passwordEncoder.matches(command.newPassword(), currentPasswordHash)) {
            throw new InvalidPasswordException("New password must differ from old password");
        }
    }
}
