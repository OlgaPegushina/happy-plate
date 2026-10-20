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

class CategoryUpdateDtoValidationTest {

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
        var dto = new CategoryUpdateDto(null, null);

        Set<ConstraintViolation<CategoryUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nameTooShortShouldFailSize() {
        var dto = new CategoryUpdateDto("A", null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void nameTooLongShouldFailSize() {
        var dto = new CategoryUpdateDto("A".repeat(121), null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void sortOrderNegativeShouldFailMin() {
        var dto = new CategoryUpdateDto(null, -1);

        var violations = validator.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("sortOrder");
    }
}
