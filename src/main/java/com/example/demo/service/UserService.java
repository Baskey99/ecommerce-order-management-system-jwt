package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.dto.UserRegisterDTO;
import java.util.List;

public interface UserService {
    UserDTO register(UserRegisterDTO dto);
    UserDTO login(UserLoginDTO dto);
    UserDTO getUserById(Long id);
    UserDTO getUserByUsername(String username);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO dto);
    void deleteUser(Long id);
}
