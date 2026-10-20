package delivery.service.publisher;

import interaction.api.event.DeliveryDeliveredEvent;
import interaction.api.event.DeliveryTakenEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static interaction.api.utility.AppConstants.EXCHANGE_EVENTS;
import static interaction.api.utility.AppConstants.RK_DELIVERY_DELIVERED;
import static interaction.api.utility.AppConstants.RK_DELIVERY_TAKEN;

@Component
@RequiredArgsConstructor
public class DeliveryEventsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTaken(UUID orderId) {
        var event = new DeliveryTakenEvent(
                UUID.randomUUID(),
                Instant.now(),
                1,
                orderId
        );

        rabbitTemplate.convertAndSend(EXCHANGE_EVENTS, RK_DELIVERY_TAKEN, event);
    }

    public void publishDelivered(UUID orderId) {
        var event = new DeliveryDeliveredEvent(
                UUID.randomUUID(),
                Instant.now(),
                1,
                orderId
        );

        rabbitTemplate.convertAndSend(EXCHANGE_EVENTS, RK_DELIVERY_DELIVERED, event);
    }
}