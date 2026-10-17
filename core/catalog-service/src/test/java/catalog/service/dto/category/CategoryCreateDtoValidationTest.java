package catalog.service.dto.category;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryCreateDtoValidationTest {

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
        var dto = new CategoryCreateDto("Soups", 0);

        Set<ConstraintViolation<CategoryCreateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nameBlankShouldFailNotBlank() {
        var dto = new CategoryCreateDto("   ", 0);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void nameTooShortShouldFailSize() {
        var dto = new CategoryCreateDto("A", 0);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void nameTooLongShouldFailSize() {
        var dto = new CategoryCreateDto("A".repeat(121), 0);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void sortOrderNegativeShouldFailMin() {
        var dto = new CategoryCreateDto("Soups", -1);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("sortOrder");
    }
}
