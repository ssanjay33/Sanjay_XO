package com.xo.eventmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private String role;

    public JwtAuthResponse(String accessToken, Long userId, String name, String email, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
