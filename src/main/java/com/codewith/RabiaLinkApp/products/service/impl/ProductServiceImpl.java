package com.codewith.RabiaLinkApp.products.service.impl;

import com.codewith.RabiaLinkApp.products.domain.Product;
import com.codewith.RabiaLinkApp.products.repository.ProductRepository;
import com.codewith.RabiaLinkApp.products.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
