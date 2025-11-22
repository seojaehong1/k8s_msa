package com.example.product.messaging;

import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageConsumer {

    @Autowired
    private ProductRepository productRepository;

    @RabbitListener(queues = "order.created")
    public void handleOrderCreated(OrderDTO order) {
        System.out.println("주문 생성 이벤트 수신: " + order.getId());

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItemDTO item : order.getItems()) {
                productRepository.findById(item.getProductId())
                    .ifPresent(product -> {
                        int newStock = product.getStock() - item.getQuantity();

                        if (newStock < 0) {
                            System.err.println("재고 부족: " + product.getName());
                            return;
                        }

                        product.setStock(newStock);
                        productRepository.save(product);

                        System.out.println("재고 감소: " + product.getName() +
                                         " (" + item.getQuantity() + "개) -> 남은 재고: " + newStock);
                    });
            }
        }
    }
}

