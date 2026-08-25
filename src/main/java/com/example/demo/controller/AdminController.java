package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin management endpoints")
@Slf4j
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Only admin can view all users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        log.info("Admin fetching all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse(true, "Users retrieved successfully", users));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", description = "Only admin can view specific user")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        log.info("Admin fetching user with id: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse(true, "User retrieved successfully", user));
    }

    @GetMapping("/orders")
    @Operation(summary = "Get all orders", description = "Only admin can view all orders")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Admin fetching all orders with page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get order by ID", description = "Only admin can view specific order")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long id) {
        log.info("Admin fetching order with id: {}", id);
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Order retrieved successfully", order));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user", description = "Only admin can delete users")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        log.info("Admin deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully", null));
    }
}
