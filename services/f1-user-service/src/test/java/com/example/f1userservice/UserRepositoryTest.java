package com.example.f1userservice;

import com.example.f1userservice.model.User;
import com.example.f1userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUsername - existing user returns user")
    void findByUsername_existingUser_returnsUser() {
        User user = User.builder()
                .username("alonso")
                .email("fernando@aston.com")
                .password("encoded")
                .role(User.Role.USER)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByUsername("alonso");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("fernando@aston.com");
    }

    @Test
    @DisplayName("findByUsername - non-existing user returns empty")
    void findByUsername_nonExisting_returnsEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername - duplicate check works")
    void existsByUsername_duplicateCheck() {
        User user = User.builder()
                .username("bottas")
                .email("valtteri@sauber.com")
                .password("encoded")
                .role(User.Role.USER)
                .build();
        entityManager.persist(user);

        assertThat(userRepository.existsByUsername("bottas")).isTrue();
        assertThat(userRepository.existsByUsername("zhou")).isFalse();
    }

    @Test
    @DisplayName("existsByEmail - email duplicate check works")
    void existsByEmail_duplicateCheck() {
        User user = User.builder()
                .username("ocon")
                .email("esteban@alpine.com")
                .password("encoded")
                .role(User.Role.USER)
                .build();
        entityManager.persist(user);

        assertThat(userRepository.existsByEmail("esteban@alpine.com")).isTrue();
        assertThat(userRepository.existsByEmail("new@email.com")).isFalse();
    }

    @Test
    @DisplayName("findByEmail - existing email returns user")
    void findByEmail_existingEmail_returnsUser() {
        User user = User.builder()
                .username("gasly")
                .email("pierre@alpine.com")
                .password("encoded")
                .role(User.Role.USER)
                .build();
        entityManager.persist(user);

        Optional<User> found = userRepository.findByEmail("pierre@alpine.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("gasly");
    }

    @Test
    @DisplayName("save - user is persisted with generated id")
    void save_userPersistedWithId() {
        User user = User.builder()
                .username("magnussen")
                .email("kevin@haas.com")
                .password("encoded")
                .role(User.Role.USER)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("magnussen");
    }
}
