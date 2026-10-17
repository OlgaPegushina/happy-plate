package catalog.service.model;

import catalog.service.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

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
class RestaurantPersistenceConstraintsTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void saveNullNameShouldFail() {
        Restaurant r = new Restaurant(null, null, null, null);

        assertThatThrownBy(() -> restaurantRepository.saveAndFlush(r))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveNameTooLongShouldFail() {
        Restaurant r = new Restaurant("A".repeat(201), null, null, null);

        assertThatThrownBy(() -> restaurantRepository.saveAndFlush(r))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveDescriptionTooLongShouldFail() {
        Restaurant r = new Restaurant("Test", "A".repeat(1001), null, null);

        assertThatThrownBy(() -> restaurantRepository.saveAndFlush(r))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void savePhoneTooLongShouldFail() {
        Restaurant r = new Restaurant("Test", null, "1".repeat(13), null);

        assertThatThrownBy(() -> restaurantRepository.saveAndFlush(r))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveAddressTooLongShouldFail() {
        Restaurant r = new Restaurant("Test", null, null, "A".repeat(501));

        assertThatThrownBy(() -> restaurantRepository.saveAndFlush(r))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
