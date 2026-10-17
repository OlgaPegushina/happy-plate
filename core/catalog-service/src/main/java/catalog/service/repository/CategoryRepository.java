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
    List<MenuCategory> findAllByRestaurant_Id(UUID restaurantId, Sort sort);

    boolean existsByRestaurant_IdAndNameIgnoreCase(UUID restaurantId, String name);

    boolean existsByRestaurant_IdAndNameIgnoreCaseAndIdNot(UUID restaurantId, String name, UUID id);

    Optional<MenuCategory> findByIdAndRestaurant_Id(UUID id, UUID restaurantId);
}
