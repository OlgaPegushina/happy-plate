package delivery.service.listener;

import delivery.service.publisher.DeliveryEventsPublisher;
import delivery.service.service.OrderStatusService;
import interaction.api.event.PaymentPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

import static interaction.api.utility.AppConstants.Q_DELIVERY_PAYMENT_PAID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryPaymentPaidListener {

    private final OrderStatusService orderStatusService;

    private final DeliveryEventsPublisher publisher;

    @RabbitListener(queues = Q_DELIVERY_PAYMENT_PAID)
    public void onPaymentPaid(PaymentPaidEvent event) {
        log.info("Got PaymentPaidEvent: orderId={}", event.orderId());

        orderStatusService.markDeliveryTaken(event.orderId());
        publisher.publishTaken(event.orderId());

        sleepImitatingDelivery();

        orderStatusService.markDelivered(event.orderId());
        publisher.publishDelivered(event.orderId());

        log.info("Delivery flow done: orderId={}", event.orderId());
    }

    private void sleepImitatingDelivery() {
        try {
            Thread.sleep(1000 + ThreadLocalRandom.current().nextInt(500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}