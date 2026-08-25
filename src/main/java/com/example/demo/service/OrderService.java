package com.example.demo.service;

import com.example.demo.dto.OrderCreateDTO;
import com.example.demo.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDTO createOrder(OrderCreateDTO dto, Long userId);
    OrderDTO getOrderById(Long id);
    Page<OrderDTO> getUserOrders(Long userId, Pageable pageable);
    Page<OrderDTO> getUserOrdersByUsername(String username, Pageable pageable);
    Page<OrderDTO> getAllOrders(Pageable pageable);
    OrderDTO updateOrderStatus(Long id, String status);
    void cancelOrder(Long id);
}
