package catalog.service.dto.menu;

import interaction.api.dto.MoneyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MenuItemCreateDto(
        @NotBlank
        @Size(min = 2, max = 200)
        String name,

        @Size(max = 2000)
        String description,

        UUID categoryId,

        @NotNull
        @Valid
        MoneyDto price,

        @Min(0)
        int sortOrder
) {}
