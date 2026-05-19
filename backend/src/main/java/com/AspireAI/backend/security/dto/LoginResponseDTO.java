package com.AspireAI.backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String token;
    private long expiresIn;
    private UserData user;

    @Data
    @Builder
    public static class UserData {
        private UUID id;
        private String email;
        private String fullName;
        private String role;
    }
}
