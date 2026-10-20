package interaction.api.event;

import java.time.Instant;
import java.util.UUID;

public record DeliveryDeliveredEvent(
        UUID eventId,
        Instant occurredAt,
        int version,
        UUID orderId) {
}
