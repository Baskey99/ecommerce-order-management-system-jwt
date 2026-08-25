package com.example.demo.service.impl;

import com.example.demo.dto.OrderCreateDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemCreateDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.OrderNotFoundException;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public OrderDTO createOrder(OrderCreateDTO dto, Long userId) {
        log.info("Creating order for user id: {}", userId);

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Order order = new Order();
        order.setUser(user);
        // Use status from DTO if provided, otherwise default to PENDING
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemCreateDTO itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Product not found with id: " + itemDto.getProductId()));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(product.getPrice());

            order.getItems().add(orderItem);

            // Update product stock
            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);

            // Calculate total price
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with id: {}", savedOrder.getId());
        return convertToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.debug("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        log.info("Fetching orders for user id: {}", userId);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return orderRepository.findByUserId(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getUserOrdersByUsername(String username, Pageable pageable) {
        log.info("Fetching orders for user with username: {}", username);

        // Verify user exists by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        return orderRepository.findByUserId(user.getId(), pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.info("Fetching all orders with pagination");
        return orderRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, String status) {
        log.info("Updating order status for id: {} to: {}", id, status);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        try {
            order.setStatus(OrderStatus.valueOf(status));
            Order updatedOrder = orderRepository.save(order);
            log.info("Order status updated successfully for id: {}", id);
            return convertToDTO(updatedOrder);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }
    }

    @Override
    public void cancelOrder(Long id) {
        log.info("Cancelling order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }

        // Restore product stocks
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order cancelled successfully with id: {}", id);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<OrderItemDTO> items = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setProductId(item.getProduct().getId());
                itemDTO.setProductName(item.getProduct().getName());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPrice(item.getPrice());
                items.add(itemDTO);
            }
            dto.setItems(items);
        }

        return dto;
    }
}
