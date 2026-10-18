package order.service.messaging;

import java.util.UUID;

public record OrderCreatedMessage(UUID orderId) {
}