package catalog.service.mapper.internal;

import interaction.api.dto.MenuItemSnapshotDto;
import interaction.api.dto.MoneyDto;
import catalog.service.model.MenuItem;
import org.springframework.stereotype.Component;

@Component
public class MenuItemInternalMapperImpl implements MenuItemInternalMapper {
    @Override
    public MenuItemSnapshotDto toSnapshot(MenuItem mi) {
        return new MenuItemSnapshotDto(
                mi.getId(),
                mi.getRestaurant().getId(),
                mi.getName(),
                new MoneyDto(mi.getPrice().getAmount(), mi.getPrice().getCurrency()),
                mi.isAvailable()
        );
    }
}
