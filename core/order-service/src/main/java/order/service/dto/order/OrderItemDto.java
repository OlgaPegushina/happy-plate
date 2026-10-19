package order.service.dto.order;

import interaction.api.dto.MoneyDto;

import java.util.UUID;

public record OrderItemDto(
        UUID id,
        UUID menuItemId,
        String name,
        MoneyDto unitPrice,
        int quantity
) {}