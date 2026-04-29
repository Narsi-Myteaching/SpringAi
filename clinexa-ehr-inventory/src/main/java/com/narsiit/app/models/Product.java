package com.narsiit.app.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "inventory")
public class Product {
  @Id
  @Column(name = "product_id", length = 100)
  private String productId;

  @Column(name = "name", length = 500)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "price", precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "supplier_price", precision = 10, scale = 2)
  private BigDecimal supplierPrice;

  @Column(name = "supplier", length = 500)
  private String supplier;

  @Column(name = "quantity")
  private Integer quantity;

  @Column(name = "rating", precision = 2, scale = 1)
  private BigDecimal rating;

  @Column(name = "brand", length = 100)
  private String brand;

  @Column(name = "category", length = 100)
  private String category;
}