package catalog.service.dto.restaurant;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RestaurantDto(
        UUID id,
        String name,
        String description,
        String phone,
        String address,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
