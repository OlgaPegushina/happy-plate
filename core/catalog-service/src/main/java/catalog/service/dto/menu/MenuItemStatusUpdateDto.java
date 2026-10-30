package catalog.service.dto.menu;

import jakarta.validation.constraints.NotNull;

public record MenuItemStatusUpdateDto(
        @NotNull
        Boolean available
) {}
