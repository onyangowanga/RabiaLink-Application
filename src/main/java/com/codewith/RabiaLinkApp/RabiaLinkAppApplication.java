package com.codewith.RabiaLinkApp;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.codewith.RabiaLinkApp.products.domain.Product;
import com.codewith.RabiaLinkApp.products.repository.ProductRepository;

@SpringBootApplication
public class RabiaLinkAppApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(RabiaLinkAppApplication.class, args);
	}
	
	
}
