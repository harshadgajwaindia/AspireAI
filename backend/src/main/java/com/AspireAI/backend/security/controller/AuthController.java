package com.AspireAI.backend.security.controller;

import com.AspireAI.backend.security.dto.LoginRequestDTO;
import com.AspireAI.backend.security.dto.LoginResponseDTO;
import com.AspireAI.backend.security.dto.RegisterRequestDTO;
import com.AspireAI.backend.security.dto.UserResponseDTO;
import com.AspireAI.backend.security.entity.User;
import com.AspireAI.backend.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequestDTO registerUserDto) {
        System.out.println("SIGNUP REQUEST RECEIVED for: " + registerUserDto.getEmail());
        User registeredUser = authService.signup(registerUserDto);
        
        UserResponseDTO response = UserResponseDTO.builder()
                .id(registeredUser.getId())
                .email(registeredUser.getEmail())
                .fullName(registeredUser.getFullName())
                .role(registeredUser.getRole().name())
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(@RequestBody LoginRequestDTO loginUserDto) {
        LoginResponseDTO loginResponse = authService.authenticate(loginUserDto);
        return ResponseEntity.ok(loginResponse);
    }
}
