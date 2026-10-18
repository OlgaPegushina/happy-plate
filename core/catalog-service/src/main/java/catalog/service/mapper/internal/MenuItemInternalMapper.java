package catalog.service.mapper.internal;

import catalog.service.dto.internal.MenuItemSnapshotDto;
import catalog.service.model.MenuItem;

public interface MenuItemInternalMapper {
    MenuItemSnapshotDto toSnapshot(MenuItem mi);
}
