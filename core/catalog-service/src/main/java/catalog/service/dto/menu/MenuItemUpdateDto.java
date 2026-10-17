package catalog.service.dto.menu;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MenuItemUpdateDto(
        @Size(min = 2, max = 200)
        String name,

        @Size(max = 2000)
        String description,

        UUID categoryId,

        @Valid
        MoneyDto price,

        @Min(0)
        Integer sortOrder
) {}
