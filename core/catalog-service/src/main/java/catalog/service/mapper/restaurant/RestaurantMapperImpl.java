package catalog.service.mapper.restaurant;

import catalog.service.model.Restaurant;
import catalog.service.dto.restaurant.RestaurantDto;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapperImpl implements RestaurantMapper {
    @Override
    public RestaurantDto toRestaurantDto(Restaurant restaurant) {

        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getPhone(),
                restaurant.getAddress(),
                restaurant.isActive(),
                restaurant.getCreatedAt(),
                restaurant.getUpdatedAt()
        );
    }
}
