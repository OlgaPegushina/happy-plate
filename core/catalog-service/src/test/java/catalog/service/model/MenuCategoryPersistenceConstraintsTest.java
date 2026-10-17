package catalog.service.model;

import catalog.service.repository.CategoryRepository;
import catalog.service.repository.RestaurantRepository;
import org.springframework.core.env.Environment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DataJpaTest(properties = {
        // -- выдёргиваем config-server
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
class MenuCategoryPersistenceConstraintsTest {
    @Autowired
    Environment env;

    @Test
    void printProfiles() {
        System.out.println("active profiles -> "
                           + Arrays.toString(env.getActiveProfiles()));
        System.out.println("eureka.client.enabled = "
                           + env.getProperty("eureka.client.enabled"));
    }

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void saveNullRestaurantShouldFail() {
        var category = new MenuCategory(null, "Soups", 0);

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(category))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveNullNameShouldFail() {
        Restaurant restaurant = persistRestaurant();

        var category = new MenuCategory(restaurant, null, 0);

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(category))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveNameTooLongShouldFail() {
        Restaurant restaurant = persistRestaurant();

        var category = new MenuCategory(restaurant, "A".repeat(121), 0);

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(category))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Restaurant persistRestaurant() {
        Restaurant r = new Restaurant("Test Restaurant", null, null, null);
        // -- active по умолчанию true
        return restaurantRepository.saveAndFlush(r);
    }
}