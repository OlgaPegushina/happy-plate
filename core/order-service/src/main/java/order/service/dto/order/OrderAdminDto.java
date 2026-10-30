package order.service.dto.order;

import interaction.api.dto.MoneyDto;
import interaction.api.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderAdminDto(
        UUID id,
        OrderStatus status,
        MoneyDto totalPrice,
        OffsetDateTime createdAt
) {}
