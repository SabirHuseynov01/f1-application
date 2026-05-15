package com.example.f1userservice;

import com.example.f1userservice.controller.AuthController;
import com.example.f1userservice.dto.*;
import com.example.f1userservice.exception.InvalidCredentialsException;
import com.example.f1userservice.exception.UserAlreadyExistsException;
import com.example.f1userservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.example.f1userservice.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /register - valid request returns token")
    void register_validRequest_returnsAuthResponse() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("maxverstappen", "max@redbull.com", "password123");
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt.token.here")
                .username("maxverstappen")
                .role("USER")
                .build();

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", is("jwt.token.here")))
                .andExpect(jsonPath("$.data.username", is("maxverstappen")));
    }

    @Test
    @DisplayName("POST /register - duplicate username returns 409")
    void register_duplicateUsername_returnsConflict() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("existinguser", "test@test.com", "password123");

        when(authService.register(any())).thenThrow(
                new UserAlreadyExistsException("Username already taken: existinguser"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already taken")));
    }

    @Test
    @DisplayName("POST /register - invalid email returns 400")
    void register_invalidEmail_returnsBadRequest() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("user", "invalid-email", "pass");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login - valid credentials returns token")
    void login_validCredentials_returnsToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("hamilton", "mercedes123");
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt.hamilton.token")
                .username("hamilton")
                .role("USER")
                .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", is("jwt.hamilton.token")))
                .andExpect(jsonPath("$.data.role", is("USER")));
    }

    @Test
    @DisplayName("POST /login - invalid credentials returns 401")
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("wrong", "wrong");

        when(authService.login(any())).thenThrow(
                new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Invalid")));
    }

    @Test
    @DisplayName("POST /logout - valid token blacklists it")
    void logout_validToken_returnsSuccess() throws Exception {
        doNothing().when(authService).logout("valid.token.here");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid.token.here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Logged out")));
    }

    @Test
    @DisplayName("POST /validate - valid token returns validation result")
    void validate_validToken_returnsValid() throws Exception {
        TokenValidationDTO validation = TokenValidationDTO.builder()
                .valid(true)
                .username("leclerc")
                .role("USER")
                .build();

        when(authService.validateToken("valid.token")).thenReturn(validation);

        mockMvc.perform(post("/api/v1/auth/validate")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid", is(true)))
                .andExpect(jsonPath("$.data.username", is("leclerc")));
    }

    @Test
    @DisplayName("GET /profile - returns user profile")
    void getProfile_validToken_returnsProfile() throws Exception {
        TokenValidationDTO validation = TokenValidationDTO.builder()
                .valid(true)
                .username("norris")
                .role("USER")
                .build();
        UserProfileDTO profile = UserProfileDTO.builder()
                .id(1L)
                .username("norris")
                .email("lando@mclaren.com")
                .role("USER")
                .active(true)
                .build();

        when(authService.validateToken("token")).thenReturn(validation);
        when(authService.getProfile("norris")).thenReturn(profile);

        mockMvc.perform(get("/api/v1/auth/profile")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is("norris")))
                .andExpect(jsonPath("$.data.email", is("lando@mclaren.com")));
    }
}
