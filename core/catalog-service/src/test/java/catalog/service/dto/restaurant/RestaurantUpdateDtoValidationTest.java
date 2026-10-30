package catalog.service.dto.restaurant;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantUpdateDtoValidationTest {

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
    void allNullShouldBeValidForPatch() {
        var dto = new RestaurantUpdateDto(null, null, null);

        var violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void phoneInvalidFormatShouldFail() {
        var dto = new RestaurantUpdateDto(null, "123", null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("phone");
    }

    @Test
    void addressTooLongShouldFail() {
        var dto = new RestaurantUpdateDto(null, null, "A".repeat(501));

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("address");
    }
}