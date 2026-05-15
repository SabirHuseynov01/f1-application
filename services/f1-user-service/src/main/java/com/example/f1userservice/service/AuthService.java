package com.example.f1userservice.service;

import com.example.f1userservice.dto.*;
import com.example.f1userservice.exception.InvalidCredentialsException;
import com.example.f1userservice.exception.UserAlreadyExistsException;
import com.example.f1userservice.exception.UserNotFoundException;
import com.example.f1userservice.model.User;
import com.example.f1userservice.repository.UserRepository;
import com.example.f1userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .active(true)
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getUsername());

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponseDTO(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        User user =  userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        log.info("User logged in: {}", user.getUsername());
        return new AuthResponseDTO(token, user.getUsername(), user.getRole().name());
    }

    public void logout(String token) {
        long expiration = jwtUtil.getExpirationDate(token).getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "true", expiration, TimeUnit.MILLISECONDS);
        }
        log.info("User logged out, token blacklisted");
    }

    public TokenValidationDTO validateToken(String token) {
        boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
        boolean isValid = !isBlacklisted && jwtUtil.validateToken(token);

        if (isValid) {
            return new TokenValidationDTO(true, jwtUtil.extractUsername(token), jwtUtil.extractRole(token));
        }
        return new TokenValidationDTO(false, null, null);
    }

    public UserProfileDTO getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .active(user.isActive())
                .build();
    }
}
