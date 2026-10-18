package order.service.event;

import java.util.UUID;

public record OrderCreatedEvent(UUID orderId) {
}
