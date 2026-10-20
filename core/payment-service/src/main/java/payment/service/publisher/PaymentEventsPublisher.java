package payment.service.publisher;

import interaction.api.event.PaymentPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static interaction.api.utility.AppConstants.EXCHANGE_EVENTS;
import static interaction.api.utility.AppConstants.RK_PAYMENT_PAID;

@Component
@RequiredArgsConstructor
public class PaymentEventsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaid(UUID orderId) {
        var event = new PaymentPaidEvent(
                UUID.randomUUID(),
                Instant.now(),
                1,
                orderId
        );

        rabbitTemplate.convertAndSend(EXCHANGE_EVENTS, RK_PAYMENT_PAID, event);
    }
}
