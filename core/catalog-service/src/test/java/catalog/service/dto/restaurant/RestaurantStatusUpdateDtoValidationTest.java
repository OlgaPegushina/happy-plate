package catalog.service.dto.restaurant;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantStatusUpdateDtoValidationTest {

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
    void activeNullShouldFailNotNull() {
        var dto = new RestaurantStatusUpdateDto(null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("active");
    }

    @Test
    void activeNotNullShouldBeValid() {
        var dto = new RestaurantStatusUpdateDto(Boolean.TRUE);

        var violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}