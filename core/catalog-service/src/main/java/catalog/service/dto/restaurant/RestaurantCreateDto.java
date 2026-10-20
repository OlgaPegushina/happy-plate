package catalog.service.dto.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestaurantCreateDto(
        @NotBlank
        @Size(min = 2, max = 200)
        String name,

        @Size(max = 1000)
        String description,

        @Size(max = 12)
        @Pattern(regexp = "^(?:\\+7|8)\\d{10}$", message = "Invalid phone format")
        String phone,

        @Size(max = 500)
        String address
) {}
