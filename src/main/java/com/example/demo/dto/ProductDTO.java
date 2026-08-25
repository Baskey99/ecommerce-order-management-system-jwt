package com.example.demo.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private Boolean active = true;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, String description, BigDecimal price, Integer stock, Boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class ProductDTO {
//     private Long id;

//     @NotBlank(message = "Product name is required")
//     private String name;

//     @NotBlank(message = "Description is required")
//     private String description;

//     @NotNull(message = "Price is required")
//     private BigDecimal price;

//     @NotNull(message = "Stock is required")
//     private Integer stock;

//     private Boolean active;
//     private Date createdAt;
//     private Date updatedAt;
// }
