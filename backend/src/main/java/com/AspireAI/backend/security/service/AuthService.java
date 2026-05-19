package com.AspireAI.backend.security.service;

import com.AspireAI.backend.security.dto.LoginRequestDTO;
import com.AspireAI.backend.security.dto.LoginResponseDTO;
import com.AspireAI.backend.security.dto.RegisterRequestDTO;
import com.AspireAI.backend.security.entity.User;
import com.AspireAI.backend.security.exception.EmailAlreadyExistsException;
import com.AspireAI.backend.security.jwt.JwtUtils;
import com.AspireAI.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public User signup(RegisterRequestDTO input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .fullName(input.getFullName())
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .role(User.Role.USER)
                .build();

        return userRepository.save(user);
    }

    public LoginResponseDTO authenticate(LoginRequestDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        User authenticatedUser = userRepository.findByEmail(input.getEmail())
                .orElseThrow();

        String jwtToken = jwtUtils.generateToken(authenticatedUser);

        return LoginResponseDTO.builder()
                .token(jwtToken)
                .expiresIn(jwtUtils.getExpirationTime())
                .user(LoginResponseDTO.UserData.builder()
                        .id(authenticatedUser.getId())
                        .email(authenticatedUser.getEmail())
                        .fullName(authenticatedUser.getFullName())
                        .role(authenticatedUser.getRole().name())
                        .build())
                .build();
    }
}
