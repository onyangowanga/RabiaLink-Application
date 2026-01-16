package com.codewith.RabiaLinkApp.products.service;


import java.util.List;

import com.codewith.RabiaLinkApp.products.domain.Product;

public interface ProductService {

    Product createProduct(Product product);

    List<Product> getAllProducts();
}

