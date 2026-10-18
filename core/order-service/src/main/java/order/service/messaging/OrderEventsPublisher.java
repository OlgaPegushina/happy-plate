package order.service.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderEventsPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.orders.exchange}")
    private String exchange;

    @Value("${app.rabbit.orders.created-routing-key}")
    private String createdRoutingKey;

    public void publishOrderCreated(UUID orderId) {
        rabbitTemplate.convertAndSend(exchange, createdRoutingKey, new OrderCreatedMessage(orderId));
    }
}