package com.example.product.messaging;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Double totalPrice;
    private String customerName;
    private String customerEmail;
    private LocalDateTime orderDate;
    private String status;
    private Long storeId;
    private LocalDateTime estimatedCompletionTime;
    private List<OrderItemDTO> items;
}

