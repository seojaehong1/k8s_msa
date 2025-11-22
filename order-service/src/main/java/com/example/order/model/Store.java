package com.example.order.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "STORES")
@Data
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String location;
    private String phone;
    private String status;
}

