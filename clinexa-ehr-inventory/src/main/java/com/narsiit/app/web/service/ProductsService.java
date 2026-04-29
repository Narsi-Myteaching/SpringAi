package com.narsiit.app.web.service;

import com.narsiit.app.models.Product;

import java.util.List;

public interface ProductsService {

    Product createProduct(Product product);
    List<Product> getAllPorducts();
    Product getProductById(String productId);

    String generateSqlQuery(String userQuestion);

    String getProductsFromDb(String sqlQuery);
}
