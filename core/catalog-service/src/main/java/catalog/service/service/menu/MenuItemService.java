package catalog.service.service.menu;

import catalog.service.dto.menu.MenuItemCreateDto;
import catalog.service.dto.menu.MenuItemDto;
import catalog.service.dto.menu.MenuItemUpdateDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface MenuItemService {
    @Transactional
    MenuItemDto create(UUID restaurantId, MenuItemCreateDto req);

    @Transactional(readOnly = true)
    MenuItemDto get(UUID restaurantId, UUID id);

    @Transactional(readOnly = true)
    List<MenuItemDto> getAll(UUID restaurantId, Boolean available, UUID categoryId);

    @Transactional
    MenuItemDto update(UUID restaurantId, UUID id, MenuItemUpdateDto req);

    @Transactional
    MenuItemDto updateAvailableStatus(UUID restaurantId, UUID id, boolean newAvailableStatus);

    @Transactional
    MenuItemDto clearCategory(UUID restaurantId, UUID id);
}
