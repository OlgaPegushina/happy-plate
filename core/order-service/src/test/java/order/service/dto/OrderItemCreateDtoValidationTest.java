package order.service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import order.service.dto.order.OrderItemCreateDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemCreateDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void quantityZeroOrNegativeShouldFail(int badQty) {
        var dto = new OrderItemCreateDto(UUID.randomUUID(), badQty);
        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 100})
    void quantityPositiveShouldPass(int goodQty) {
        var dto = new OrderItemCreateDto(UUID.randomUUID(), goodQty);
        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void nullMenuItemIdShouldFail(boolean ignored) {
        var dto = new OrderItemCreateDto(null, 1);
        assertThat(validator.validate(dto)).isNotEmpty();
    }
}
