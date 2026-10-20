package order.service.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import interaction.api.dto.MenuItemIdsRequest;
import interaction.api.dto.MenuItemSnapshotDto;
import interaction.api.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import order.service.dto.order.OrderCreateDto;
import order.service.dto.order.OrderItemCreateDto;
import order.service.dto.order.OrderDto;
import interaction.api.enums.OrderStatus;
import order.service.exception.ConflictException;
import order.service.exception.NotFoundException;
import order.service.feign.CatalogClient;
import order.service.mapper.OrderMapper;
import order.service.model.Money;
import order.service.model.Order;
import order.service.model.OrderItem;
import order.service.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final CatalogClient catalogClient;

    private final OrderMapper orderMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public OrderDto createDraft(OrderCreateDto req) {
        var normalized = normalizeItems(req.items());
        var ids = normalized.keySet().stream().toList();

        var catalogItems = fetchCatalogItems(ids);
        var byId = indexById(catalogItems);

        validateAllFound(ids, byId);
        validateAvailability(catalogItems);
        validateRestaurant(req.restaurantId(), catalogItems);
        var currency = validateSingleCurrency(catalogItems);

        var order = buildDraftOrder(req, normalized, byId, currency);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    @Override
    public OrderDto updateStatus(UUID orderId, OrderStatus targetStatus) {
        var order = getOrder(orderId);
        var current = order.getStatus();

        // -- повтор статуса не ломает обработку
        if (current == targetStatus) {
            return orderMapper.toDto(order);
        }

        // --Откат в DRAFT запрещаем всегда
        if (targetStatus == OrderStatus.DRAFT) {
            throw new ConflictException("Order status can't be reverted to DRAFT, current=" + current);
        }

        return switch (targetStatus) {
            case CREATED -> submitIfDraft(order); // DRAFT в CREATED
            case PAID -> setPaid(order);
            case DELIVERY_TAKEN -> setDeliveryTaken(order);
            case DELIVERED -> setDelivered(order);
            default -> throw new ConflictException("Unsupported target status: " + targetStatus);
        };
    }

    @Transactional(readOnly = true)
    @Override
    public OrderDto getById(UUID orderId) {
        var order = getOrder(orderId);
        return orderMapper.toDto(order);
    }

    private OrderDto setPaid(Order order) {
        var current = order.getStatus();

        if (current != OrderStatus.CREATED) {
            throw new ConflictException("Only CREATED order can be marked as PAID, current=" + current);
        }

        order.setStatus(OrderStatus.PAID);

        return orderMapper.toDto(order);
    }

    private OrderDto setDeliveryTaken(Order order) {
        var current = order.getStatus();

        if (current != OrderStatus.PAID) {
            throw new ConflictException("Only PAID order can be marked as DELIVERY_TAKEN, current=" + current);
        }

        order.setStatus(OrderStatus.DELIVERY_TAKEN);

        return orderMapper.toDto(order);
    }

    private OrderDto setDelivered(Order order) {
        var current = order.getStatus();

        if (current != OrderStatus.DELIVERY_TAKEN) {
            throw new ConflictException("Only DELIVERY_TAKEN order can be marked as DELIVERED, current=" + current);
        }

        order.setStatus(OrderStatus.DELIVERED);

        return orderMapper.toDto(order);
    }

    private Order getOrder(UUID orderId) {
        return orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    private OrderDto submitIfDraft(Order order) {
        if (order.getStatus() == OrderStatus.CREATED) {
            return orderMapper.toDto(order);
        }
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new ConflictException("Only DRAFT order can be submitted, current=" + order.getStatus());
        }
        var ids = order.getItems().stream()
                .map(OrderItem::getMenuItemId)
                .distinct()
                .toList();

        var catalogItems = fetchCatalogItems(ids);
        validateAvailability(catalogItems);
        validateRestaurant(order.getRestaurantId(), catalogItems);
        order.setStatus(OrderStatus.CREATED);

        eventPublisher.publishEvent(new OrderCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                1,
                order.getId()
        ));
        return orderMapper.toDto(order);
    }

    private Map<UUID, Integer> normalizeItems(List<OrderItemCreateDto> items) {
        Map<UUID, Integer> result = new LinkedHashMap<>();
        for (var it : items) {
            result.merge(it.menuItemId(), it.quantity(), Integer::sum);
        }
        return result;
    }

    private List<MenuItemSnapshotDto> fetchCatalogItems(List<UUID> ids) {
        return catalogClient.getMenuItemsByIds(new MenuItemIdsRequest(ids));
    }

    private Map<UUID, MenuItemSnapshotDto> indexById(List<MenuItemSnapshotDto> items) {
        return items.stream().collect(Collectors.toMap(MenuItemSnapshotDto::id, it -> it));
    }

    private void validateAllFound(List<UUID> requestedIds, Map<UUID, MenuItemSnapshotDto> byId) {
        var missing = requestedIds.stream().filter(id -> !byId.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            throw new NotFoundException("Menu items not found: " + missing);
        }
    }

    private void validateAvailability(List<MenuItemSnapshotDto> items) {
        var unavailable = items.stream().filter(i -> !i.available()).map(MenuItemSnapshotDto::id).toList();
        if (!unavailable.isEmpty()) {
            throw new ConflictException("Menu items are not available: " + unavailable);
        }
    }

    private void validateRestaurant(UUID restaurantId, List<MenuItemSnapshotDto> items) {
        var another = items.stream()
                .filter(i -> !restaurantId.equals(i.restaurantId()))
                .map(MenuItemSnapshotDto::id)
                .toList();
        if (!another.isEmpty()) {
            throw new ConflictException("Menu items belong to another restaurant: " + another);
        }
    }

    private String validateSingleCurrency(List<MenuItemSnapshotDto> items) {
        var currency = items.getFirst().price().currency();
        boolean multi = items.stream().anyMatch(i -> !currency.equals(i.price().currency()));
        if (multi) {
            throw new ConflictException("Multi-currency order is not allowed");
        }
        return currency;
    }

    private Order buildDraftOrder(
            OrderCreateDto req,
            Map<UUID, Integer> normalized,
            Map<UUID, MenuItemSnapshotDto> byId,
            String currency
    ) {
        Order order = new Order();
        order.setCustomerId(req.customerId());
        order.setRestaurantId(req.restaurantId());
        order.setStatus(OrderStatus.DRAFT);
        BigDecimal total = BigDecimal.ZERO;
        for (var e : normalized.entrySet()) {
            var menuItemId = e.getKey();
            var qty = e.getValue();
            var mi = byId.get(menuItemId);
            var unit = mi.price().amount();

            total = total.add(unit.multiply(BigDecimal.valueOf(qty)));
            OrderItem oi = new OrderItem();
            oi.setMenuItemId(mi.id());
            oi.setNameSnapshot(mi.name());
            oi.setUnitPriceSnapshot(new Money(unit, currency));
            oi.setQuantity(qty);
            order.addItem(oi);
        }
        order.setTotal(new Money(total, currency));
        return order;
    }
}
