package catalog.service.repository;

import catalog.service.model.MenuItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    @EntityGraph(attributePaths = {"restaurant", "category"})
    @Query("""
        select mi from MenuItem mi
        where mi.restaurant.id = :restaurantId
          and (:available is null or mi.available = :available)
          and (:categoryId is null or mi.category.id = :categoryId)
        order by mi.sortOrder asc, mi.name asc
    """)
    List<MenuItem> findForRestaurant(UUID restaurantId, Boolean available, UUID categoryId);

    @EntityGraph(attributePaths = {"restaurant", "category"})
    Optional<MenuItem> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
