package catalog.service.dto.restaurant;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantCreateDtoValidationTest {

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
    void validShouldHaveNoViolations() {
        var dto = new RestaurantCreateDto(
                "My Restaurant",
                "Nice place",
                "+79998887766",
                "Moscow"
        );

        Set<ConstraintViolation<RestaurantCreateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nameBlankShouldFail() {
        var dto = new RestaurantCreateDto("   ", null, null, null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void nameTooShortShouldFail() {
        var dto = new RestaurantCreateDto("A", null, null, null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void nameTooLongShouldFail() {
        var dto = new RestaurantCreateDto("A".repeat(201), null, null, null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void phoneInvalidFormatShouldFailPattern() {
        var dto = new RestaurantCreateDto("Test", null, "123", null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("phone");
    }

    @Test
    void phoneTooLongShouldFailSize() {
        var dto = new RestaurantCreateDto("Test", null, "+7" + "1".repeat(20), null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("phone");
    }

    @Test
    void descriptionTooLongShouldFailSize() {
        var dto = new RestaurantCreateDto("Test", "A".repeat(1001), null, null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }

    @Test
    void addressTooLongShouldFailSize() {
        var dto = new RestaurantCreateDto("Test", null, null, "A".repeat(501));

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("address");
    }
}
