package delivery.service.service;

import java.util.UUID;

public interface OrderStatusService {
    void markDeliveryTaken(UUID orderId);

    void markDelivered(UUID orderId);
}
