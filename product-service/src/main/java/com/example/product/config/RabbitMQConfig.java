package com.example.product.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATED_QUEUE = "order.created";
    public static final String ORDER_STATUS_CHANGED_QUEUE = "order.status.changed";
    public static final String INVENTORY_UPDATED_QUEUE = "inventory.updated";
    public static final String EXCHANGE_NAME = "coffee-shop-exchange";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("coffee-shop-exchange");
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("order.created");
    }

    @Bean
    public Binding bindingOrderCreated(Queue orderCreatedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(exchange).with("order.created");
    }

    @Bean
    public Queue orderStatusChangedQueue() {
        return new Queue(ORDER_STATUS_CHANGED_QUEUE, true);
    }

    @Bean
    public Queue inventoryUpdatedQueue() {
        return new Queue(INVENTORY_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding orderStatusChangedBinding(Queue orderStatusChangedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(orderStatusChangedQueue)
                .to(exchange)
                .with(ORDER_STATUS_CHANGED_QUEUE);
    }

    @Bean
    public Binding inventoryUpdatedBinding(Queue inventoryUpdatedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(inventoryUpdatedQueue)
                .to(exchange)
                .with(INVENTORY_UPDATED_QUEUE);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

