package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductDTO dto);
    ProductDTO getProductById(Long id);
    Page<ProductDTO> getAllProducts(Pageable pageable);
    Page<ProductDTO> searchProducts(String keyword, Pageable pageable);
    ProductDTO updateProduct(Long id, ProductDTO dto);
    void deleteProduct(Long id);
}
