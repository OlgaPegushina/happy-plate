package order.service.messaging;

import lombok.RequiredArgsConstructor;
import order.service.event.OrderCreatedEvent;
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
