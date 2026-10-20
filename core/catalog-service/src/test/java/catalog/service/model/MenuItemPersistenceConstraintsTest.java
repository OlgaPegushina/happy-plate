package catalog.service.model;

import catalog.service.repository.CategoryRepository;
import catalog.service.repository.MenuItemRepository;
import catalog.service.repository.RestaurantRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=" +
        "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration," +
        "org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration"
})
class MenuItemPersistenceConstraintsTest {

    @Autowired
    private MenuItemRepository menuItemRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Restaurant saveRestaurant() {
        return restaurantRepository.save(new Restaurant(
                "Pizza & Co",
                "pizza@example.com", null, null
        ));
    }

    private MenuCategory saveCategory(Restaurant r) {
        return categoryRepository.save(new MenuCategory(r, "Main", 0));
    }

    private MenuItem buildValidItem() {
        Restaurant r = saveRestaurant();
        MenuCategory c = saveCategory(r);
        return new MenuItem(
                r,
                c,
                "Margarita",
                "Classic pizza",
                Money.of(new BigDecimal("8.50"), "EUR")
        );
    }

    @Test
    void saveNullRestaurantShouldFail() {
        MenuItem item = new MenuItem(
                null,
                null,
                "Nameless",
                "Desc",
                Money.of(new BigDecimal("3.00"), "EUR")
        );

        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(item))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveNullNameShouldFail() {
        MenuItem item = buildValidItem();
        item.setName(null);

        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(item))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveTooLongDescriptionShouldFail() {
        MenuItem item = buildValidItem();
        item.setDescription("A".repeat(2001));

        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(item))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveNegativeSortOrderShouldFail() {
        MenuItem item = buildValidItem();
        item.setSortOrder(-1);

        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(item))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                            .anyMatch(v -> v.getPropertyPath().toString().equals("sortOrder"));
                });
    }

    @Test
    void saveNegativePriceShouldFail() {
        MenuItem item = buildValidItem();
        item.setPrice(Money.of(new BigDecimal("-5.00"), "EUR"));

        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(item))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().equals("price.amount")
                                    && v.getMessage().contains("Price amount must be non-negative")
                            );
                });
    }

    @Test
    void saveValidItemShouldPass() {
        MenuItem saved = menuItemRepository.saveAndFlush(buildValidItem());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Margarita");
        assertThat(saved.getPrice().getAmount()).isEqualByComparingTo("8.50");
    }

    @Test
    void saveNullCurrencyShouldFail() {
        MenuItem item = buildValidItem();
        item.setPrice(new Money(new BigDecimal("1.00"), null));

        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(item))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                            .anyMatch(v -> v.getPropertyPath().toString().equals("price.currency"));
                });
    }

    @Test
    void saveInvalidCurrencyShouldFail() {
        MenuItem item = buildValidItem();
        item.setPrice(new Money(new BigDecimal("1.00"), "EU"));

        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(item))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    ConstraintViolationException cve = (ConstraintViolationException) ex;
                    assertThat(cve.getConstraintViolations())
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().equals("price.currency")
                                    && v.getMessage().contains("Currency must be 3 uppercase letters")
                            );
                });
    }
}
