package com.wise.petadoption.user.service;

import com.wise.petadoption.user.common.Role;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.wise.petadoption.support.TestFixtures.user;
import static com.wise.petadoption.support.TestFixtures.userEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserEntityMapper entityMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void injectEntityManager() {
        ReflectionTestUtils.setField(userService, "entityManager", entityManager);
    }

    @Test
    void create_NewEmail_PersistsUserWithEncodedPassword() {
        CreateUserCommand command = new CreateUserCommand("jane@example.com", "raw-password",
                "Jane", "Doe", "+15551234567", Role.ROLE_USER);
        UserEntity mappedEntity = userEntity(null, "jane@example.com", Role.ROLE_USER);
        UserEntity savedEntity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        User expected = user(1L, "jane@example.com", Role.ROLE_USER);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(entityMapper.toEntity(command)).thenReturn(mappedEntity);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(userRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(entityMapper.toModel(savedEntity)).thenReturn(expected);

        User result = userService.create(command);

        assertThat(result).isEqualTo(expected);
        assertThat(mappedEntity.getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void create_DuplicateEmail_ThrowsEmailAlreadyExistsException() {
        CreateUserCommand command = new CreateUserCommand("taken@example.com", "raw-password",
                "Jane", "Doe", "+15551234567", Role.ROLE_USER);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(command))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_EmailUnchanged_SkipsEmailUniquenessCheck() {
        UserEntity entity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        UpdateProfileCommand command = new UpdateProfileCommand(1L, "jane@example.com",
                "Janet", "Doe", "+15550000000");
        User expected = user(1L, "jane@example.com", Role.ROLE_USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(expected);

        userService.updateProfile(command);

        assertThat(entity.getFirstName()).isEqualTo("Janet");
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateProfile_EmailChangedToAvailable_UpdatesProfile() {
        UserEntity entity = userEntity(1L, "old@example.com", Role.ROLE_USER);
        UpdateProfileCommand command = new UpdateProfileCommand(1L, "new@example.com",
                "Jane", "Doe", "+15550000000");
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(entityMapper.toModel(entity)).thenReturn(user(1L, "new@example.com", Role.ROLE_USER));

        userService.updateProfile(command);

        assertThat(entity.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).existsByEmail("new@example.com");
    }

    @Test
    void updateProfile_EmailChangedToTaken_ThrowsEmailAlreadyExistsException() {
        UserEntity entity = userEntity(1L, "old@example.com", Role.ROLE_USER);
        UpdateProfileCommand command = new UpdateProfileCommand(1L, "taken@example.com",
                "Jane", "Doe", "+15550000000");
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(command))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updateProfile_UnknownUser_ThrowsUserNotFoundException() {
        UpdateProfileCommand command = new UpdateProfileCommand(99L, "jane@example.com",
                "Jane", "Doe", "+15550000000");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(command))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void changePassword_ValidOldPasswordAndDifferentNew_UpdatesPasswordHash() {
        UserEntity entity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        entity.setPasswordHash("current-hash");
        ChangePasswordCommand command = new ChangePasswordCommand(1L, "old-pass", "new-pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("old-pass", "current-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-pass", "current-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");

        userService.changePassword(command);

        assertThat(entity.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void changePassword_OldPasswordMismatch_ThrowsInvalidPasswordException() {
        UserEntity entity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        entity.setPasswordHash("current-hash");
        ChangePasswordCommand command = new ChangePasswordCommand(1L, "wrong-pass", "new-pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("wrong-pass", "current-hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(command))
                .isInstanceOf(InvalidPasswordException.class);
        assertThat(entity.getPasswordHash()).isEqualTo("current-hash");
    }

    @Test
    void changePassword_NewPasswordEqualsOld_ThrowsInvalidPasswordException() {
        UserEntity entity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        entity.setPasswordHash("current-hash");
        ChangePasswordCommand command = new ChangePasswordCommand(1L, "same-pass", "same-pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("same-pass", "current-hash")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(command))
                .isInstanceOf(InvalidPasswordException.class);
        assertThat(entity.getPasswordHash()).isEqualTo("current-hash");
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        UserEntity entity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        User expected = user(1L, "jane@example.com", Role.ROLE_USER);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(expected);

        assertThat(userService.findByEmail("jane@example.com")).isEqualTo(expected);
    }

    @Test
    void findByEmail_UnknownEmail_ThrowsUserNotFoundException() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("ghost@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findById_UnknownId_ThrowsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void delete_ExistingId_DeletesUser() {
        UserEntity entity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        userService.delete(1L);

        verify(userRepository).delete(entity);
    }

    @Test
    void delete_UnknownId_ThrowsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).delete(any());
    }
}
