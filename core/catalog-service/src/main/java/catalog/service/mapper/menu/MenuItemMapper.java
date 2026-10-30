package catalog.service.mapper.menu;

import catalog.service.model.MenuItem;
import catalog.service.dto.menu.MenuItemDto;

public interface MenuItemMapper {
    MenuItemDto toMenuItemDto(MenuItem menuItem);
}
