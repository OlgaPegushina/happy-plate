package catalog.service.model;

import catalog.service.dto.menu.MoneyDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    void validMoneyDto_shouldPass() {
        MoneyDto dto = new MoneyDto(new BigDecimal("9.99"), "USD");
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void negativeAmount_shouldFail() {
        MoneyDto dto = new MoneyDto(new BigDecimal("-1.00"), "USD");

        Set<ConstraintViolation<MoneyDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be greater than or equal to 0.00");
    }

    @Test
    void wrongCurrency_shouldFail() {
        MoneyDto dto = new MoneyDto(new BigDecimal("10.00"), "usd");

        Set<ConstraintViolation<MoneyDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("^[A-Z]{3}$");
    }
}