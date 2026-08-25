package com.example.demo.dto;

import com.example.demo.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;


public class OrderCreateDTO {
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemCreateDTO> items;

    @Schema(
        description = "Order status - PENDING: Order created, awaiting confirmation | CONFIRMED: Order confirmed by admin | SHIPPED: Order shipped to customer | DELIVERED: Order delivered to customer | CANCELLED: Order cancelled",
        example = "PENDING",
        allowableValues = {"PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"}
    )
    private OrderStatus status;

    public OrderCreateDTO() {}

    public OrderCreateDTO(List<OrderItemCreateDTO> items) {
        this.items = items;
        this.status = OrderStatus.PENDING;
    }

    public OrderCreateDTO(List<OrderItemCreateDTO> items, OrderStatus status) {
        this.items = items;
        this.status = status;
    }

    public List<OrderItemCreateDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemCreateDTO> items) {
        this.items = items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

