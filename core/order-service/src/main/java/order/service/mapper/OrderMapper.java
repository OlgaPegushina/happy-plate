package order.service.mapper;

import order.service.dto.order.OrderDto;
import order.service.model.Order;

public interface OrderMapper {
    OrderDto toDto(Order order);
}