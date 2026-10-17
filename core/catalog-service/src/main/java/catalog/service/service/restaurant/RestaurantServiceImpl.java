package catalog.service.service.restaurant;

import catalog.service.model.Restaurant;
import catalog.service.dto.restaurant.RestaurantCreateDto;
import catalog.service.dto.restaurant.RestaurantDto;
import catalog.service.dto.restaurant.RestaurantUpdateDto;
import catalog.service.exception.ConflictException;
import catalog.service.exception.NotFoundException;
import catalog.service.mapper.restaurant.RestaurantMapper;
import catalog.service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    private final RestaurantMapper restaurantMapper;

    @Transactional
    @Override
    public RestaurantDto create(RestaurantCreateDto req) {
        if (restaurantRepository.existsByNameIgnoreCase(req.name())) {
            throw new ConflictException("Restaurant name already exists: " + req.name());
        }
        Restaurant r = new Restaurant(req.name(), req.description(), req.phone(), req.address());
        Restaurant saved = restaurantRepository.saveAndFlush(r);
        return restaurantMapper.toRestaurantDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public RestaurantDto get(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + id));

        return restaurantMapper.toRestaurantDto(restaurant);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RestaurantDto> getAll(Boolean active) {
        Sort sort = Sort.by("name").ascending();
        List<Restaurant> list = (active == null) ? restaurantRepository.findAll(sort) : restaurantRepository.findAllByActive(active, sort);
        return list.stream()
                .map(restaurantMapper::toRestaurantDto)
                .toList();
    }

    @Transactional
    @Override
    public RestaurantDto update(UUID id, RestaurantUpdateDto req) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + id));

        if (req.description() != null) restaurant.setDescription(req.description());
        if (req.phone() != null) restaurant.setPhone(req.phone());
        if (req.address() != null) restaurant.setAddress(req.address());

        restaurantRepository.flush();

        return restaurantMapper.toRestaurantDto(restaurant);
    }

    @Transactional
    @Override
    public RestaurantDto updateStatus(UUID id, boolean newStatus) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + id));

        restaurant.setActive(newStatus);

        restaurantRepository.flush();

        return restaurantMapper.toRestaurantDto(restaurant);
    }
}
