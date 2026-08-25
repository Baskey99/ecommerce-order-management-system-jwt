package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.OrderCreateDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management endpoints")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "User can create orders - User ID is automatically fetched from logged-in user")
    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        // Get currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get user ID from username
        Long userId = userService.getUserByUsername(username).getId();
        
        log.info("Creating order for logged-in user: {} (ID: {})", username, userId);
        
        // Create new DTO with userId set (from JWT token)
        OrderCreateDTO orderDto = new OrderCreateDTO(dto.getItems(), dto.getStatus());
        
        OrderDTO createdOrder = orderService.createOrder(orderDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Order created successfully", createdOrder));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by ID")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long id) {
        log.info("Fetching order with id: {}", id);
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Order retrieved successfully", order));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user orders", description = "Retrieve all orders for a specific user")
    public ResponseEntity<ApiResponse> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching orders for user id: {} with page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getUserOrders(userId, pageable);

        return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get logged-in user's orders", description = "Retrieve all orders for the currently logged-in user")
    public ResponseEntity<ApiResponse> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Get currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Fetching orders for logged-in user: {} with page: {}, size: {}", username, page, size);

        try {
            // Fetch orders by username
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orders = orderService.getUserOrdersByUsername(username, pageable);

            return ResponseEntity.ok(new ApiResponse(true, "Your orders retrieved successfully", orders));
        } catch (Exception e) {
            log.error("Error fetching orders for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error fetching orders: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        log.info("Updating order status for id: {} to: {}", id, status);
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", updatedOrder));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order", description = "Cancel an existing order")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long id) {
        log.info("Cancelling order with id: {}", id);
        orderService.cancelOrder(id);
        return ResponseEntity.ok(new ApiResponse(true, "Order cancelled successfully", null));
    }
}

