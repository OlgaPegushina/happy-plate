package catalog.service.repository;

import catalog.service.model.MenuCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<MenuCategory, UUID> {
    @EntityGraph(value = "category-restaurant")
    List<MenuCategory> findAllByRestaurantId(UUID restaurantId, Sort sort);

    boolean existsByRestaurantIdAndNameIgnoreCase(UUID restaurantId, String name);

    boolean existsByRestaurantIdAndNameIgnoreCaseAndIdNot(UUID restaurantId, String name, UUID id);

    Optional<MenuCategory> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
