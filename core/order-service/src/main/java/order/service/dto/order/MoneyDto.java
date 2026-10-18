package order.service.dto.order;

import java.math.BigDecimal;

public record MoneyDto(
        BigDecimal amount,
        String currency
) {}
