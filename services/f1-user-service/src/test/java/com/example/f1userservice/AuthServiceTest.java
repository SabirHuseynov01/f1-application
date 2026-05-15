package com.example.f1userservice;

import com.example.f1userservice.dto.*;
import com.example.f1userservice.exception.InvalidCredentialsException;
import com.example.f1userservice.exception.UserAlreadyExistsException;
import com.example.f1userservice.exception.UserNotFoundException;
import com.example.f1userservice.model.User;
import com.example.f1userservice.repository.UserRepository;
import com.example.f1userservice.security.JwtUtil;
import com.example.f1userservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("register - new user creates account and returns token")
    void register_newUser_success() {
        RegisterRequestDTO request = new RegisterRequestDTO("perez", "checo@rb.com", "password123");

        when(userRepository.existsByUsername("perez")).thenReturn(false);
        when(userRepository.existsByEmail("checo@rb.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken("perez", "USER")).thenReturn("jwt.perez");

        AuthResponseDTO result = authService.register(request);

        assertThat(result.getToken()).isEqualTo("jwt.perez");
        assertThat(result.getUsername()).isEqualTo("perez");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - duplicate username throws exception")
    void register_duplicateUsername_throws() {
        RegisterRequestDTO request = new RegisterRequestDTO("existing", "email@test.com", "pass");
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    @DisplayName("register - duplicate email throws exception")
    void register_duplicateEmail_throws() {
        RegisterRequestDTO request = new RegisterRequestDTO("newuser", "existing@test.com", "pass");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    @DisplayName("login - valid credentials returns token")
    void login_validCredentials_success() {
        LoginRequestDTO request = new LoginRequestDTO("hamilton", "mercedes123");
        User user = User.builder()
                .id(1L)
                .username("hamilton")
                .password("encoded_pass")
                .role(User.Role.USER)
                .build();

        when(userRepository.findByUsername("hamilton")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mercedes123", "encoded_pass")).thenReturn(true);
        when(jwtUtil.generateToken("hamilton", "USER")).thenReturn("jwt.hamilton");

        AuthResponseDTO result = authService.login(request);

        assertThat(result.getToken()).isEqualTo("jwt.hamilton");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("login - wrong password throws exception")
    void login_wrongPassword_throws() {
        LoginRequestDTO request = new LoginRequestDTO("hamilton", "wrongpass");
        User user = User.builder().username("hamilton").password("encoded").build();

        when(userRepository.findByUsername("hamilton")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login - non-existing user throws exception")
    void login_nonExistingUser_throws() {
        LoginRequestDTO request = new LoginRequestDTO("unknown", "pass");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("logout - adds token to blacklist")
    void logout_validToken_blacklists() {
        when(jwtUtil.getExpirationDate("token")).thenReturn(
                new java.util.Date(System.currentTimeMillis() + 3600000));

        authService.logout("token");

        verify(valueOperations).set(eq("blacklist:token"), eq("true"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("validateToken - non-blacklisted valid token returns valid")
    void validateToken_validAndNotBlacklisted_returnsValid() {
        when(redisTemplate.hasKey("blacklist:token")).thenReturn(false);
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("leclerc");
        when(jwtUtil.extractRole("token")).thenReturn("USER");

        TokenValidationDTO result = authService.validateToken("token");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getUsername()).isEqualTo("leclerc");
    }

    @Test
    @DisplayName("validateToken - blacklisted token returns invalid")
    void validateToken_blacklisted_returnsInvalid() {
        when(redisTemplate.hasKey("blacklist:token")).thenReturn(true);

        TokenValidationDTO result = authService.validateToken("token");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    @DisplayName("validateToken - invalid token returns invalid")
    void validateToken_invalid_returnsInvalid() {
        when(redisTemplate.hasKey("blacklist:token")).thenReturn(false);
        when(jwtUtil.validateToken("token")).thenReturn(false);

        TokenValidationDTO result = authService.validateToken("token");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    @DisplayName("getProfile - existing user returns profile")
    void getProfile_existingUser_returnsProfile() {
        User user = User.builder()
                .id(1L)
                .username("sainz")
                .email("carlos@ferrari.com")
                .role(User.Role.USER)
                .active(true)
                .build();

        when(userRepository.findByUsername("sainz")).thenReturn(Optional.of(user));

        UserProfileDTO result = authService.getProfile("sainz");

        assertThat(result.getUsername()).isEqualTo("sainz");
        assertThat(result.getEmail()).isEqualTo("carlos@ferrari.com");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("getProfile - non-existing user throws exception")
    void getProfile_nonExisting_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getProfile("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
