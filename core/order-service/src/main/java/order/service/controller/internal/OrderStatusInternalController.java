package order.service.controller.internal;

import interaction.api.dto.OrderStatusUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderStatusInternalController {
    private final OrderService orderService;

    @PutMapping("/internal/orders/{orderId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(@PathVariable UUID orderId,
                             @RequestBody @Valid OrderStatusUpdateDto request) {
        log.info("[internal] update status orderId={} -> {}", orderId, request.status());
        orderService.internalUpdateStatus(orderId, request.status());
    }
}
