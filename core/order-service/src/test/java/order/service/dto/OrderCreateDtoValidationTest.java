package order.service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import order.service.dto.order.OrderCreateDto;
import order.service.dto.order.OrderItemCreateDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreateDtoValidationTest {

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

    @Test
    void nullRestaurantIdShouldFail() {
        var dto = new OrderCreateDto(
                null,
                List.of(new OrderItemCreateDto(UUID.randomUUID(), 1))
        );

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void nullItemsShouldFail() {
        var dto = new OrderCreateDto(
                UUID.randomUUID(),
                null
        );

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void emptyItemsShouldFail() {
        var dto = new OrderCreateDto(
                UUID.randomUUID(),
                List.of()
        );

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void invalidItemInsideItemsShouldFail() {
        var dto = new OrderCreateDto(
                UUID.randomUUID(),
                List.of(new OrderItemCreateDto(null, 0)) // @NotNull + @Positive
        );

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void validCreateShouldPass() {
        var dto = new OrderCreateDto(
                UUID.randomUUID(),
                List.of(new OrderItemCreateDto(UUID.randomUUID(), 2))
        );

        assertThat(validator.validate(dto)).isEmpty();
    }
}