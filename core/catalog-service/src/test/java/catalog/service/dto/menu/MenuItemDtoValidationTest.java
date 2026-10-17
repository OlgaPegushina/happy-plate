package catalog.service.dto.menu;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemDtoValidationTest {

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

    private static Stream<String> invalidNames() {
        return Stream.of("A", "A".repeat(201));
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void nameTooShortOrLongOnCreateShouldFail(String badName) {
        MenuItemCreateDto dto = new MenuItemCreateDto(
                badName,
                "desc",
                UUID.randomUUID(),
                new MoneyDto(new BigDecimal("5.00"), "EUR"),
                0
        );

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void nameTooShortOrLongOnUpdateShouldFail(String badName) {
        MenuItemUpdateDto dto = new MenuItemUpdateDto(
                badName,
                null,
                null,
                null,
                null
        );

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void statusUpdateValidShouldPass(boolean status) {
        MenuItemStatusUpdateDto dto = new MenuItemStatusUpdateDto(status);

        assertThat(validator.validate(dto)).isEmpty();
    }
}
