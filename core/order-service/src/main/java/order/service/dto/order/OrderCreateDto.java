package order.service.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderCreateDto(
        @NotNull(message = "RestaurantId must not be null")
        UUID restaurantId,

        @NotEmpty(message = "Items must not be empty")
        @Valid
        List<OrderItemCreateDto> items
) {
}
