package order.service.listener;

import interaction.api.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import order.service.publisher.OrderEventsPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class OrderEventsListener {

    private final OrderEventsPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        publisher.publishOrderCreated(event.orderId());
    }
}
