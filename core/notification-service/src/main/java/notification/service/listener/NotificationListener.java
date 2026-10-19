package notification.service.listener;

import interaction.api.event.DeliveryDeliveredEvent;
import interaction.api.event.DeliveryTakenEvent;
import interaction.api.event.OrderCreatedEvent;
import interaction.api.event.PaymentPaidEvent;
import lombok.extern.slf4j.Slf4j;
import notification.service.config.NotificationAmqpConfig;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(queues = NotificationAmqpConfig.Q_NOTIFICATIONS)
public class NotificationListener {

    @RabbitHandler
    public void onOrderCreated(OrderCreatedEvent e,
                               @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String rk) {
        log.info("Notify: {}, order created, orderId={}", rk, e.orderId());
    }

    @RabbitHandler
    public void onPaymentPaid(PaymentPaidEvent e,
                              @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String rk) {
        log.info("Notify: {}, payment paid, orderId={}", rk, e.orderId());
    }

    @RabbitHandler
    public void onDeliveryTaken(DeliveryTakenEvent e,
                                @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String rk) {
        log.info("Notify: {}, delivery taken, orderId={}", rk, e.orderId());
    }

    @RabbitHandler
    public void onDeliveryDelivered(DeliveryDeliveredEvent e,
                                    @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String rk) {
        log.info("Notify: {}, delivered, orderId={}", rk, e.orderId());
    }

    @RabbitHandler(isDefault = true)
    public void onUnknown(Object payload,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String rk) {
        log.warn("Notify: unknown event. rk={}, payloadClass={}", rk,
                payload == null ? "null" : payload.getClass().getName());
    }
}