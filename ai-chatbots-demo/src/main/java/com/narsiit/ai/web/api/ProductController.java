package com.narsiit.ai.web.api;

import com.narsiit.ai.model.Product;
import com.narsiit.ai.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
  private final ProductRepository productRepository;

  @GetMapping("/{id}")
  public Optional<Product> getProductById(@PathVariable("id") String productId) {
    return productRepository.findById(productId);
  }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }
}
