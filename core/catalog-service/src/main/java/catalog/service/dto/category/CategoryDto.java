package catalog.service.dto.category;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryDto(
        UUID id,
        UUID restaurantId,
        String name,
        int sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
