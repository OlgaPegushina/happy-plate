package catalog.service.dto.restaurant;

import jakarta.validation.constraints.NotNull;

public record RestaurantStatusUpdateDto(
        @NotNull
        Boolean active
) {}
