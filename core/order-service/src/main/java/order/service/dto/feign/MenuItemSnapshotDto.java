package order.service.dto.feign;

import order.service.dto.order.MoneyDto;

import java.util.UUID;

public record MenuItemSnapshotDto(
        UUID id,
        UUID restaurantId,
        String name,
        MoneyDto price,
        boolean available
) {}