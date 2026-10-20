package order.service.model;

import interaction.api.enums.OrderStatus;
import order.service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DataJpaTest(properties = {
        // -- config-server
        "spring.config.import=",
        "spring.cloud.config.enabled=false",

        // -- глушим всё, что к Eureka
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",

        // -- на всякий случай полностью исключаем автоконфиг
        "spring.autoconfigure.exclude=" +
        "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration," +
        "org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration"
})
class OrderPersistenceConstraintsTest {

    @Autowired
    Environment env;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void printProfiles() {
        System.out.println("active profiles -> " + Arrays.toString(env.getActiveProfiles()));
        System.out.println("eureka.client.enabled = " + env.getProperty("eureka.client.enabled"));
    }

    @Test
    void saveNullCustomerIdShouldFail() {
        var order = baseOrder();
        order.setCustomerId(null);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveNullRestaurantIdShouldFail() {
        var order = baseOrder();
        order.setRestaurantId(null);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveNullStatusShouldFail() {
        var order = baseOrder();
        order.setStatus(null);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveNullTotalShouldFail() {
        var order = baseOrder();
        order.setTotal(null);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveTotalNullAmountShouldFail() {
        var order = baseOrder();
        order.setTotal(new Money(null, "EUR"));

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveTotalNullCurrencyShouldFail() {
        var order = baseOrder();
        order.setTotal(new Money(new BigDecimal("10.00"), null));

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveTotalCurrencyTooLongShouldFail() {
        var order = baseOrder();
        order.setTotal(new Money(new BigDecimal("10.00"), "EURO"));

        assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Order baseOrder() {
        var order = new Order();
        order.setCustomerId(UUID.randomUUID());
        order.setRestaurantId(UUID.randomUUID());
        order.setStatus(OrderStatus.DRAFT);
        order.setTotal(new Money(new BigDecimal("10.00"), "EUR"));
        return order;
    }
}
