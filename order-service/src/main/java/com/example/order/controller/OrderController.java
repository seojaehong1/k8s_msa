package com.example.order.controller;

import com.example.order.messaging.OrderMessageProducer;
import com.example.order.model.Order;
import com.example.order.model.OrderItem;
import com.example.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMessageProducer messageProducer;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        
        // 추가: 준비시간 계산
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            int maxPreparationTime = order.getItems().stream()
                .mapToInt(OrderItem::getPreparationTime)
                .max()
                .orElse(0);
            
            order.setEstimatedCompletionTime(
                order.getOrderDate().plusMinutes(maxPreparationTime)
            );
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // 이벤트 발행 추가
        messageProducer.sendOrderCreatedEvent(savedOrder);
        
        return savedOrder;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    order.setId(id);
                    return ResponseEntity.ok(orderRepository.save(order));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    orderRepository.delete(order);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/store/{storeId}")
    public List<Order> getOrdersByStore(@PathVariable Long storeId) {
        return orderRepository.findByStoreId(storeId);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody String status) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setStatus(status);
                    Order updatedOrder = orderRepository.save(order);
                    
                    // 이벤트 발행 추가
                    messageProducer.sendOrderStatusChangedEvent(id, status);
                    
                    return ResponseEntity.ok(updatedOrder);
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 