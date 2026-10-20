package catalog.service.dto.menu;

import interaction.api.dto.MoneyDto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MenuItemDto(
        UUID id,
        UUID restaurantId,
        UUID categoryId,
        String name,
        String description,
        MoneyDto price,
        boolean available,
        int sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
