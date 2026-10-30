package order.service.service;

import order.service.dto.order.OrderAdminDto;
import order.service.dto.order.OrderCreateDto;
import order.service.dto.order.OrderDto;
import interaction.api.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    OrderDto createDraft(OrderCreateDto req);

    OrderDto adminUpdateStatus(UUID orderId, OrderStatus targetStatus);

    void internalUpdateStatus(UUID orderId, OrderStatus targetStatus);

    OrderDto getById(UUID orderId);

    Page<OrderAdminDto> getOrdersForAdmin(OrderStatus status, Pageable pageable);
}