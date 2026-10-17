package catalog.service.dto.category;

import java.util.UUID;

public record CategoryDto(
        UUID id,
        UUID restaurantId,
        String name,
        int sortOrder
) {}
