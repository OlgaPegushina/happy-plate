package catalog.service.service.internal;

import catalog.service.dto.internal.MenuItemSnapshotDto;
import java.util.List;
import java.util.UUID;

public interface MenuItemInternalService {

    List<MenuItemSnapshotDto> getSnapshotsByIds(List<UUID> ids);

}
