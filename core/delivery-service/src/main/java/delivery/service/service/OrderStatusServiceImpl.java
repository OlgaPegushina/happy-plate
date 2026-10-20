package delivery.service.service;

import delivery.service.feign.OrderStatusClient;
import interaction.api.dto.OrderStatusUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static interaction.api.enums.OrderStatus.DELIVERED;
import static interaction.api.enums.OrderStatus.DELIVERY_TAKEN;

@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderStatusClient client;

    @Override
    public void markDeliveryTaken(UUID orderId) {
        client.updateStatus(orderId, new OrderStatusUpdateDto(DELIVERY_TAKEN));
    }

    @Override
    public void markDelivered(UUID orderId) {
        client.updateStatus(orderId, new OrderStatusUpdateDto(DELIVERED));
    }
}
