package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Only admin can create products")
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody ProductDTO dto) {
        log.info("Creating product: {}", dto.getName());
        ProductDTO createdProduct = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Product created successfully", createdProduct));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve product details by ID")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        log.info("Fetching product with id: {}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", product));
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products with pagination and sorting")
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        log.info("Fetching all products with page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ProductDTO> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", products));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Searching products with keyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchProducts(keyword, pageable);

        return ResponseEntity.ok(new ApiResponse(true, "Search completed successfully", products));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Only admin can update products")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        log.info("Updating product with id: {}", id);
        ProductDTO updatedProduct = productService.updateProduct(id, dto);
        return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", updatedProduct));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Only admin can delete products")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product with id: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully", null));
    }
}
