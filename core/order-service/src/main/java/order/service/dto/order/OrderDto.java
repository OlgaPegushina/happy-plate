package order.service.dto.order;

import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID customerId,
        UUID restaurantId,
        String status,
        MoneyDto total,
        List<OrderItemDto> items
) {
}
