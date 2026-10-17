package catalog.service.mapper.menu;

import catalog.service.model.MenuItem;
import catalog.service.dto.menu.MenuItemDto;
import catalog.service.dto.menu.MoneyDto;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MenuItemMapperImpl implements MenuItemMapper {
    @Override
    public MenuItemDto toMenuItemDto(MenuItem menuItem) {

        UUID categoryId = (menuItem.getCategory() == null) ? null : menuItem.getCategory().getId();
        MoneyDto price = new MoneyDto(menuItem.getPrice().getAmount(), menuItem.getPrice().getCurrency());

        return new MenuItemDto(
                menuItem.getId(),
                menuItem.getRestaurant().getId(),
                categoryId,
                menuItem.getName(),
                menuItem.getDescription(),
                price,
                menuItem.isAvailable(),
                menuItem.getSortOrder(),
                menuItem.getCreatedAt(),
                menuItem.getUpdatedAt()
        );
    }
}
