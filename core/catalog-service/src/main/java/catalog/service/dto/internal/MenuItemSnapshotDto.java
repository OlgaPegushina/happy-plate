package catalog.service.dto.internal;

import catalog.service.dto.menu.MoneyDto;

import java.util.UUID;

public record MenuItemSnapshotDto(
        UUID id,
        UUID restaurantId,
        String name,
        MoneyDto price,
        boolean available
) {}
