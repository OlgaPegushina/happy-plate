package interaction.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MenuItemSnapshotDto(
        @NotNull UUID id,
        @NotNull UUID restaurantId,
        @NotBlank String name,
        @NotNull @Valid MoneyDto price,
        boolean available
) {}
