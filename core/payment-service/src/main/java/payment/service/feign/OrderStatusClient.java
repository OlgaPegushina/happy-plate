package payment.service.feign;

import interaction.api.dto.OrderStatusUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import payment.service.config.FeignInternalTokenConfig;

import java.util.UUID;

@FeignClient(
        name = "order-service", configuration = FeignInternalTokenConfig.class
)
public interface OrderStatusClient {

    @PutMapping("/internal/orders/{orderId}/status")
    void updateStatus(@PathVariable("orderId") UUID orderId,
                      @RequestBody OrderStatusUpdateDto request);
}
