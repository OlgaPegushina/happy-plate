package order.service.repository;

import java.util.Optional;
import java.util.UUID;

import interaction.api.enums.OrderStatus;
import order.service.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(UUID id);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsByIdAndCustomerId(UUID id, UUID customerId);

    boolean existsByIdAndCustomerId(UUID id, UUID customerId);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
}
