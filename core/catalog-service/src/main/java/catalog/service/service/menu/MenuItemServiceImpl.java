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
        Restaurant restaurant = getRestaurant(restaurantId);

        MenuCategory category = null;
        if (req.categoryId() != null) {
            category = getCategory(restaurantId, req);
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
        MenuItem item = getMenuItem(restaurantId, id);

        return menuItemMapper.toMenuItemDto(item);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MenuItemDto> getAll(UUID restaurantId, Boolean available, UUID categoryId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant not found: " + restaurantId);
        }

        // если фильтруем по categoryId — проверим, что категория этого ресторана
        if (categoryId != null && categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId).isEmpty()) {
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
        MenuItem item = getMenuItem(restaurantId, id);

        updateItemFields(restaurantId, req, item);

        menuItemRepository.flush();
        return menuItemMapper.toMenuItemDto(item);
    }

    @Transactional
    @Override
    public MenuItemDto updateAvailableStatus(UUID restaurantId, UUID id, boolean newAvailableStatus) {
        MenuItem item = getMenuItem(restaurantId, id);

        item.setAvailable(newAvailableStatus);

        menuItemRepository.flush();

        return menuItemMapper.toMenuItemDto(item);
    }

    @Transactional
    @Override
    public MenuItemDto clearCategory(UUID restaurantId, UUID id) {
        MenuItem item = getMenuItem(restaurantId, id);

        item.setCategory(null);

        menuItemRepository.flush();

        return menuItemMapper.toMenuItemDto(item);
    }

    private MenuCategory getCategory(UUID restaurantId, MenuItemCreateDto req) {
        MenuCategory category;
        category = categoryRepository.findByIdAndRestaurantId(req.categoryId(), restaurantId)
                .orElseThrow(() -> new NotFoundException(
                        "Category not found for restaurant: " + req.categoryId()
                ));
        return category;
    }

    private MenuCategory getCategory(UUID restaurantId, MenuItemUpdateDto req) {
        return categoryRepository.findByIdAndRestaurantId(req.categoryId(), restaurantId)
                .orElseThrow(() -> new NotFoundException(
                        "Category with ID " + req.categoryId() + " not found for restaurant: " + restaurantId
                ));
    }

    private Restaurant getRestaurant(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantId));
    }

    private MenuItem getMenuItem(UUID restaurantId, UUID id) {
        return menuItemRepository.findByIdAndRestaurantId(id, restaurantId)
                .orElseThrow(() -> new NotFoundException(
                        "Menu item with ID " + id + " not found for restaurant: " + restaurantId
                ));
    }

    private void updateItemFields(UUID restaurantId, MenuItemUpdateDto req, MenuItem item) {
        if (req.name() != null) {
            item.setName(req.name());
        }

        if (req.description() != null) {
            item.setDescription(req.description());
        }

        if (req.categoryId() != null) {
            MenuCategory category = getCategory(restaurantId, req);
            item.setCategory(category);
        }

        if (req.price() != null) {
            item.setPrice(Money.of(req.price().amount(), req.price().currency()));
        }

        if (req.sortOrder() != null) {
            item.setSortOrder(req.sortOrder());
        }
    }
}