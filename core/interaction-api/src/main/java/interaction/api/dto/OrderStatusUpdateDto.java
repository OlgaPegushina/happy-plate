package interaction.api.dto;

import interaction.api.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateDto(@NotNull OrderStatus status) {}