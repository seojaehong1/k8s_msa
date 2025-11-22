package com.example.order.messaging;

import com.example.order.model.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class OrderMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "coffee-shop-exchange";

    public void sendOrderCreatedEvent(Order order) {
        rabbitTemplate.convertAndSend(
            EXCHANGE,
            "order.created",
            order
        );
        System.out.println("주문 생성 이벤트 발행: " + order.getId());
    }

    public void sendOrderStatusChangedEvent(Long orderId, String status) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("status", status);
        message.put("timestamp", LocalDateTime.now());

        rabbitTemplate.convertAndSend(
            EXCHANGE,
            "order.status.changed",
            message
        );
        System.out.println("주문 상태 변경 이벤트 발행: " + orderId + " -> " + status);
    }
}

