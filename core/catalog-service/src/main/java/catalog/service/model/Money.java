package catalog.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Money {

    @Column(name = "price_amount", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Price amount must be non-negative")
    @NotNull
    private BigDecimal amount;

    @Column(name = "price_currency", nullable = false, length = 3)
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    @NotNull
    private String currency;

    public static Money of(BigDecimal amount, String currency) {
        if (currency == null) {
            throw new IllegalArgumentException("currency must not be null");
        }
        return new Money(amount, currency.toUpperCase());
    }
}
