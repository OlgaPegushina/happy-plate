package catalog.service.service.internal;

import interaction.api.dto.MenuItemSnapshotDto;
import java.util.List;
import java.util.UUID;

public interface MenuItemInternalService {

    List<MenuItemSnapshotDto> getSnapshotsByIds(List<UUID> ids);

}
