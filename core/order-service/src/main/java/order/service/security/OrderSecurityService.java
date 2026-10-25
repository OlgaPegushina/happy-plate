package order.service.security;

import lombok.RequiredArgsConstructor;
import order.service.repository.OrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("orderSecurityService")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    private final CurrentUserService currentUserService;

    public boolean isOwner(UUID orderId, Authentication authentication) {
        UUID customerId = currentUserService.userId();
        return orderRepository.existsByIdAndCustomerId(orderId, customerId);
    }
}