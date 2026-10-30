package order.service.publisher;

import interaction.api.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static interaction.api.utility.AppConstants.EXCHANGE_EVENTS;
import static interaction.api.utility.AppConstants.RK_ORDER_CREATED;

@Component
@RequiredArgsConstructor
public class OrderEventsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(UUID orderId) {
        rabbitTemplate.convertAndSend(
                EXCHANGE_EVENTS,
                RK_ORDER_CREATED,
                new OrderCreatedEvent(
                        UUID.randomUUID(),
                        Instant.now(),
                        1,
                        orderId
                )
        );
    }
}