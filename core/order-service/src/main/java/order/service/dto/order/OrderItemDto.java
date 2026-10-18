package order.service.dto.order;

import java.util.UUID;

public record OrderItemDto(
        UUID id,
        UUID menuItemId,
        String name,
        MoneyDto unitPrice,
        int quantity
) {}