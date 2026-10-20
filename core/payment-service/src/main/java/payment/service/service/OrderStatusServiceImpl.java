package payment.service.service;

import interaction.api.dto.OrderStatusUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import payment.service.feign.OrderStatusClient;

import java.util.UUID;

import static interaction.api.enums.OrderStatus.PAID;


@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderStatusClient client;

    @Override
    public void markPaid(UUID orderId) {
        client.updateStatus(orderId, new OrderStatusUpdateDto(PAID));
    }
}
