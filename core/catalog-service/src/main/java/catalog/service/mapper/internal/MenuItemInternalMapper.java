package catalog.service.mapper.internal;

import interaction.api.dto.MenuItemSnapshotDto;
import catalog.service.model.MenuItem;

public interface MenuItemInternalMapper {
    MenuItemSnapshotDto toSnapshot(MenuItem mi);
}
