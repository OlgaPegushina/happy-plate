package catalog.service.service.restaurant;

import catalog.service.dto.restaurant.RestaurantCreateDto;
import catalog.service.dto.restaurant.RestaurantDto;
import catalog.service.dto.restaurant.RestaurantUpdateDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {
    @Transactional
    RestaurantDto create(RestaurantCreateDto req);

    @Transactional(readOnly = true)
    RestaurantDto get(UUID id);

    @Transactional(readOnly = true)
    List<RestaurantDto> getAll(Boolean active);

    @Transactional
    RestaurantDto update(UUID id, RestaurantUpdateDto req);

    @Transactional
    RestaurantDto updateStatus(UUID id, boolean newStatus);
}
