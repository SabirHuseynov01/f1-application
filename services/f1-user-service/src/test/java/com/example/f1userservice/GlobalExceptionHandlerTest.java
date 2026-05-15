package com.example.f1userservice;

import com.example.f1userservice.controller.AuthController;
import com.example.f1userservice.dto.LoginRequestDTO;
import com.example.f1userservice.dto.RegisterRequestDTO;
import com.example.f1userservice.dto.TokenValidationDTO;
import com.example.f1userservice.exception.GlobalExceptionHandler;
import com.example.f1userservice.exception.InvalidCredentialsException;
import com.example.f1userservice.exception.UserAlreadyExistsException;
import com.example.f1userservice.exception.UserNotFoundException;
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
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("UserAlreadyExistsException - returns 409 Conflict")
    void userAlreadyExists_returns409() throws Exception {
        when(authService.register(any())).thenThrow(
                new UserAlreadyExistsException("User exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequestDTO("user", "email@test.com", "pass"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("User exists")));
    }

    @Test
    @DisplayName("InvalidCredentialsException - returns 401 Unauthorized")
    void invalidCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(
                new InvalidCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequestDTO("user", "pass"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Bad credentials")));
    }

    @Test
    @DisplayName("UserNotFoundException - returns 404 Not Found")
    void userNotFound_returns404() throws Exception {
        when(authService.validateToken("token")).thenReturn(
                TokenValidationDTO.builder().valid(true).username("unknown").build());
        when(authService.getProfile("unknown")).thenThrow(
                new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/auth/profile")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    @DisplayName("Validation error - returns 400 Bad Request")
    void validationError_returns400() throws Exception {
        RegisterRequestDTO invalid = new RegisterRequestDTO("", "invalid", "123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
