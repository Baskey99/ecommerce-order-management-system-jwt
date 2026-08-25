package com.example.demo.service.impl;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO register(UserRegisterDTO dto) {
        log.info("Registering user with username: {}", dto.getUsername());

        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(Role.USER);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with username: {}", dto.getUsername());

        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public UserDTO login(UserLoginDTO dto) {
        log.info("User login attempt with username: {}", dto.getUsername());

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!user.getActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        log.info("User logged in successfully: {}", dto.getUsername());
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.debug("Fetching user with username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO dto) {
        log.info("Updating user with id: {}", id);
        
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

            long currentVersion = user.getVersion();
            log.debug("Current user version: {}", currentVersion);

            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            User updatedUser = userRepository.save(user);
            log.info("✅ User updated successfully with id: {} (version: {} → {})", 
                    id, currentVersion, updatedUser.getVersion());

            return modelMapper.map(updatedUser, UserDTO.class);

        } catch (ObjectOptimisticLockingFailureException e) {
            // Race condition detected - version conflict
            log.error("⚠️  Optimistic lock exception: Concurrent update detected for user id: {}", id);
            throw new BadRequestException(
                "User was modified by another request. Please refresh and try again (Optimistic Lock Conflict).");
        }
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        log.info("User deleted successfully with id: {}", id);
    }
}
