package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JwtAuthResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
@Slf4j
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserRegisterDTO dto) {
        log.info("User registration request for username: {}", dto.getUsername());
        UserDTO userDTO = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "User registered successfully", userDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "User login with JWT", description = "Login with username and password to get JWT token")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody UserLoginDTO dto) {
        log.info("User login request for username: {}", dto.getUsername());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Get user details
        UserDTO userDTO = userService.getUserByUsername(dto.getUsername());

        // Create response
        JwtAuthResponse authResponse = new JwtAuthResponse(token, jwtExpirationMs, userDTO);

        log.info("User {} logged in successfully with JWT token", dto.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Login successful", authResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Generate a new JWT token using current authentication")
    public ResponseEntity<ApiResponse> refreshToken() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Refresh token request without valid authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Not authenticated"));
        }

        String token = jwtTokenProvider.generateToken(authentication);
        String username = authentication.getName();
        UserDTO userDTO = userService.getUserByUsername(username);

        JwtAuthResponse authResponse = new JwtAuthResponse(token, jwtExpirationMs, userDTO);

        log.info("JWT token refreshed for user: {}", username);
        return ResponseEntity.ok(new ApiResponse(true, "Token refreshed successfully", authResponse));
    }

    // @PostMapping("/logout")
    // @Operation(summary = "User logout", description = "Logout user - Clear JWT token on client side")
    // public ResponseEntity<ApiResponse> logout() {
    //     Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
    //             .getContext().getAuthentication();

    //     if (authentication != null && authentication.isAuthenticated()) {
    //         String username = authentication.getName();
    //         log.info("User {} logged out successfully", username);
            
    //         // Clear SecurityContext
    //         org.springframework.security.core.context.SecurityContextHolder.clearContext();
            
    //         return ResponseEntity.ok(new ApiResponse(true, "Logout successful - Please delete the token from client"));
    //     }

    //     log.warn("Logout request without valid authentication");
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    //             .body(new ApiResponse(false, "No active session to logout"));
    // }
}

