package catalog.service.mapper.restaurant;

import catalog.service.model.Restaurant;
import catalog.service.dto.restaurant.RestaurantDto;

public interface RestaurantMapper {
    RestaurantDto toRestaurantDto(Restaurant restaurant);
}
