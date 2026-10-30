package order.service.model;

import interaction.api.enums.OrderStatus;
import order.service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DataJpaTest(properties = {

        "spring.config.import=",
        "spring.cloud.config.enabled=false",

        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",

        "spring.autoconfigure.exclude=" +
        "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration," +
        "org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration"
})
class OrderItemPersistenceConstraintsTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveItemWithNullMenuItemIdShouldFail() {
        var order = baseOrderWithOneItem();
        order.getItems().getFirst().setMenuItemId(null);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveItemWithNullNameSnapshotShouldFail() {
        var order = baseOrderWithOneItem();
        order.getItems().getFirst().setNameSnapshot(null);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveItemWithTooLongNameSnapshotShouldFail() {
        var order = baseOrderWithOneItem();
        order.getItems().getFirst().setNameSnapshot("A".repeat(201)); // length=200

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveItemWithNullUnitPriceSnapshotShouldFail() {
        var order = baseOrderWithOneItem();
        order.getItems().getFirst().setUnitPriceSnapshot(null);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveItemWithNullUnitPriceAmountShouldFail() {
        var order = baseOrderWithOneItem();
        order.getItems().getFirst().setUnitPriceSnapshot(new Money(null, "EUR"));

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveItemWithTooLongCurrencyShouldFail() {
        var order = baseOrderWithOneItem();
        order.getItems().getFirst().setUnitPriceSnapshot(new Money(new BigDecimal("5.00"), "EURO"));

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveItemWithNullOrderFkShouldFail() {
        var order = baseOrderWithOneItem();
        var item = order.getItems().getFirst();

        item.setOrder(null);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Order baseOrderWithOneItem() {
        var order = new Order();
        order.setCustomerId(UUID.randomUUID());
        order.setRestaurantId(UUID.randomUUID());
        order.setStatus(OrderStatus.DRAFT);
        order.setTotal(new Money(new BigDecimal("10.00"), "EUR"));

        var item = new OrderItem();
        item.setMenuItemId(UUID.randomUUID());
        item.setNameSnapshot("Burger");
        item.setUnitPriceSnapshot(new Money(new BigDecimal("5.00"), "EUR"));
        item.setQuantity(1);

        order.addItem(item);

        return order;
    }
}