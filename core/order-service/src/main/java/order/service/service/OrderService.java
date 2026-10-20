package order.service.service;

import order.service.dto.order.OrderCreateDto;
import order.service.dto.order.OrderDto;
import interaction.api.enums.OrderStatus;

import java.util.UUID;

public interface OrderService {
    OrderDto createDraft(OrderCreateDto req);

    OrderDto updateStatus(UUID orderId, OrderStatus status);

    OrderDto getById(UUID orderId);
}