package catalog.service.dto.category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateDto(
        @NotBlank
        @Size(min = 2, max = 120)
        String name,

        @Min(0)
        int sortOrder
) {}
