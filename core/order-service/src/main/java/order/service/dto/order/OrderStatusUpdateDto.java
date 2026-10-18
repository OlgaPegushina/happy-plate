package order.service.dto.order;

import jakarta.validation.constraints.NotNull;
import order.service.enums.OrderStatus;

public record OrderStatusUpdateDto(
        @NotNull OrderStatus status
) {}
