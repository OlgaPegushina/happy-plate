package payment.service.service;

import java.util.UUID;

public interface OrderStatusService {
    void markPaid(UUID orderId);

}
