package catalog.service.service.menu;

import catalog.service.model.MenuCategory;
import catalog.service.model.MenuItem;
import catalog.service.model.Money;
import catalog.service.model.Restaurant;
import catalog.service.dto.menu.MenuItemCreateDto;
import catalog.service.dto.menu.MenuItemDto;
import catalog.service.dto.menu.MenuItemUpdateDto;
import catalog.service.exception.NotFoundException;
import catalog.service.mapper.menu.MenuItemMapper;
import catalog.service.repository.CategoryRepository;
import catalog.service.repository.MenuItemRepository;
import catalog.service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final RestaurantRepository restaurantRepository;

    private final CategoryRepository categoryRepository;

    private final MenuItemRepository menuItemRepository;

    private final MenuItemMapper menuItemMapper;

    @Transactional
    @Override
    public MenuItemDto create(UUID restaurantId, MenuItemCreateDto req) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantId));

        MenuCategory category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findByIdAndRestaurant_Id(req.categoryId(), restaurantId)
                    .orElseThrow(() -> new NotFoundException(
                            "Category not found for restaurant: " + req.categoryId()
                    ));
        }

        Money money = Money.of(req.price().amount(), req.price().currency());

        MenuItem item = new MenuItem(restaurant, category, req.name(), req.description(), money);

        item.setSortOrder(req.sortOrder());

        MenuItem saved = menuItemRepository.saveAndFlush(item);

        return menuItemMapper.toMenuItemDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public MenuItemDto get(UUID restaurantId, UUID id) {
        MenuItem item = menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)
                .orElseThrow(() -> new NotFoundException("Menu item not found: " + id));

        return menuItemMapper.toMenuItemDto(item);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MenuItemDto> getAll(UUID restaurantId, Boolean available, UUID categoryId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant not found: " + restaurantId);
        }

        // если фильтруем по categoryId — проверим, что категория этого ресторана
        if (categoryId != null && categoryRepository.findByIdAndRestaurant_Id(categoryId, restaurantId).isEmpty()) {
            throw new NotFoundException("Category not found for restaurant: " + categoryId);
        }

        return menuItemRepository.findForRestaurant(restaurantId, available, categoryId)
                .stream()
                .map(menuItemMapper::toMenuItemDto)
                .toList();
    }

    @Transactional
    @Override
    public MenuItemDto update(UUID restaurantId, UUID id, MenuItemUpdateDto req) {
        MenuItem item = menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)
                .orElseThrow(() -> new NotFoundException("Menu item not found: " + id));

        if (req.name() != null) item.setName(req.name());
        if (req.description() != null) item.setDescription(req.description());

        if (req.categoryId() != null) {
            MenuCategory category = categoryRepository.findByIdAndRestaurant_Id(req.categoryId(), restaurantId)
                    .orElseThrow(() -> new NotFoundException(
                            "Category not found for restaurant: " + req.categoryId()
                    ));
            item.setCategory(category);
        }

        if (req.price() != null) {
            item.setPrice(Money.of(req.price().amount(), req.price().currency()));
        }

        if (req.sortOrder() != null) item.setSortOrder(req.sortOrder());

        menuItemRepository.flush();
        return menuItemMapper.toMenuItemDto(item);
    }

    @Transactional
    @Override
    public MenuItemDto updateAvailableStatus(UUID restaurantId, UUID id, boolean newAvailableStatus) {
        MenuItem item = menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)
                .orElseThrow(() -> new NotFoundException("Menu item not found: " + id));

        item.setAvailable(newAvailableStatus);

        menuItemRepository.flush();

        return menuItemMapper.toMenuItemDto(item);
    }

    @Transactional
    @Override
    public MenuItemDto clearCategory(UUID restaurantId, UUID id) {
        MenuItem item = menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)
                .orElseThrow(() -> new NotFoundException("Menu item not found: " + id));

        item.setCategory(null);

        menuItemRepository.flush();

        return menuItemMapper.toMenuItemDto(item);
    }
}