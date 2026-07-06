package com.wise.petadoption.user.pesistence;

import com.wise.petadoption.support.AbstractPostgresIT;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.persistence.UserEntity;
import com.wise.petadoption.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    private UserEntity newUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPhone("+15551234567");
        user.setRole(Role.ROLE_USER);
        return user;
    }

    @Test
    void findByEmail_ExistingUser_ReturnsUser() {
        entityManager.persistAndFlush(newUser("find-me@example.com"));

        Optional<UserEntity> result = userRepository.findByEmail("find-me@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void findByEmail_UnknownEmail_ReturnsEmpty() {
        assertThat(userRepository.findByEmail("nobody-here@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        entityManager.persistAndFlush(newUser("exists@example.com"));

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
    }
}
