package interaction.api.dto;

import java.util.UUID;

public record MenuItemSnapshotDto(
        UUID id,
        UUID restaurantId,
        String name,
        MoneyDto price,
        boolean available
) {}
