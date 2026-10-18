package order.service.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record OrderItemCreateDto(
        @NotNull(message = "menuItemId must not be null")
        UUID menuItemId,

        @Positive(message = "Quantity must be greater than 0")
        int quantity
) {
}
