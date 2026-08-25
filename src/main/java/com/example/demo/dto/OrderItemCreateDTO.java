package com.example.demo.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class OrderItemCreateDTO {
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    public OrderItemCreateDTO() {}

    public OrderItemCreateDTO(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class OrderItemCreateDTO {
//     @NotNull(message = "Product ID is required")
//     private Long productId;

//     @NotNull(message = "Quantity is required")
//     @Positive(message = "Quantity must be positive")
//     private Integer quantity;
// }
