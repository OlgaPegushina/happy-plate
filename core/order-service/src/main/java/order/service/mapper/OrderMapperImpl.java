package order.service.mapper;

import interaction.api.dto.MoneyDto;
import order.service.dto.order.OrderDto;
import order.service.dto.order.OrderItemDto;
import order.service.model.Money;
import order.service.model.Order;
import order.service.model.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getStatus().name(),
                toMoneyDto(order.getTotal()),
                order.getItems().stream().map(this::toItemDto).toList()
        );
    }

    private OrderItemDto toItemDto(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getMenuItemId(),
                item.getNameSnapshot(),
                toMoneyDto(item.getUnitPriceSnapshot()),
                item.getQuantity()
        );
    }

    private MoneyDto toMoneyDto(Money money) {
        return new MoneyDto(money.getAmount(), money.getCurrency());
    }
}