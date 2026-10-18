package order.service.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import order.service.dto.order.OrderCreateDto;
import order.service.dto.order.OrderItemCreateDto;
import order.service.dto.feign.MenuItemIdsRequest;
import order.service.dto.feign.MenuItemSnapshotDto;
import order.service.dto.order.OrderDto;
import order.service.enums.OrderStatus;
import order.service.event.OrderCreatedEvent;
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

        var saved = orderRepository.saveAndFlush(order);

        return orderMapper.toDto(saved);
    }

    @Transactional
    @Override
    public OrderDto updateStatus(UUID orderId, OrderStatus status) {
        var order = getOrder(orderId);

        if (status == OrderStatus.CREATED) {
            return submitIfDraft(order);
        }

        if (status == OrderStatus.DRAFT) {
            if (order.getStatus() == OrderStatus.DRAFT) {
                return orderMapper.toDto(order);
            }
            throw new ConflictException("Order status can't be reverted to DRAFT, current=" + order.getStatus());
        }

        throw new ConflictException("Unsupported status change to: " + status);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderDto getById(UUID orderId) {
        var order = getOrder(orderId);
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

        var saved = orderRepository.saveAndFlush(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(saved.getId()));

        return orderMapper.toDto(saved);
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
