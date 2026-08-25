package com.example.demo.service.impl;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(ProductDTO dto) {
        log.info("Creating product: {}", dto.getName());

        Product product = modelMapper.map(dto, Product.class);
        product.setActive(true);

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    @Cacheable(value = "products")
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");
        return productRepository.findByActiveTrue(pageable)
                .map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        log.info("Searching products with keyword: {}", keyword);
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        log.info("Updating product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {}", id);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        try {
            productRepository.delete(product);
            log.info("Product deleted successfully with id: {}", id);
        } catch (DataIntegrityViolationException e) {
            log.warn("Cannot delete product with id: {} - it is referenced by existing orders", id);
            throw new BadRequestException("Cannot delete product - it is referenced by existing orders. Please delete associated orders first.");
        }
    }
}
