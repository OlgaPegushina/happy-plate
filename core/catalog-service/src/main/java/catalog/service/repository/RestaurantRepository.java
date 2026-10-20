package catalog.service.repository;

import catalog.service.model.Restaurant;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    boolean existsByNameIgnoreCase(String name);

    List<Restaurant> findAllByActive(boolean active, Sort sort);
}