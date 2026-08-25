package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    private String token;
    private String type = "Bearer";
    private Long expiresIn;
    private UserDTO user;

    public JwtAuthResponse(String token, Long expiresIn, UserDTO user) {
        this.token = token;
        this.type = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
