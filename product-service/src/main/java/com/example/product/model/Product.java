package com.example.product.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "PRODUCTS")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private Integer preparationTime;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
} 