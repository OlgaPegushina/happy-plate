package payment.service.listener;

import interaction.api.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import payment.service.publisher.PaymentEventsPublisher;
import payment.service.service.OrderStatusService;

import static interaction.api.utility.AppConstants.Q_PAYMENT_ORDER_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOrderCreatedListener {

    private final OrderStatusService orderStatusService;

    private final PaymentEventsPublisher publisher;

    @RabbitListener(queues = Q_PAYMENT_ORDER_CREATED)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Got OrderCreatedEvent: orderId={}", event.orderId());

        try {
            orderStatusService.markPaid(event.orderId());
            publisher.publishPaid(event.orderId());
        } catch (Exception ex) {
            log.error("Payment FAILED orderId={}", event.orderId(), ex);
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("stop", ex);
        }

        log.info("Published PaymentPaidEvent: orderId={}", event.orderId());
    }
}