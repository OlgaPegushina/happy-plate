package interaction.api.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        int version,
        UUID orderId
) {}
