package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterDTO registerDTO;
    private UserLoginDTO loginDTO;
    private User user;

    @BeforeEach
    void setUp() {
        registerDTO = new UserRegisterDTO("testuser", "test@example.com", "password123", "Test", "User");
        loginDTO = new UserLoginDTO("testuser", "password123");
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.USER);
        user.setActive(true);
    }

    @Test
    void testRegisterUserSuccess() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(new UserDTO());

        UserDTO result = userService.register(registerDTO);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserDuplicateUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.register(registerDTO));
    }

    @Test
    void testLoginUserSuccess() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(new UserDTO());

        UserDTO result = userService.login(loginDTO);

        assertNotNull(result);
    }

    @Test
    void testLoginUserInvalidPassword() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login(loginDTO));
    }
}
