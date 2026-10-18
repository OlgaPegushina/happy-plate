package catalog.service.service.internal;

import catalog.service.dto.internal.MenuItemSnapshotDto;
import catalog.service.mapper.internal.MenuItemInternalMapper;
import catalog.service.model.MenuItem;
import catalog.service.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MenuItemInternalServiceImpl implements MenuItemInternalService {

    private final MenuItemRepository menuItemRepository;

    private final MenuItemInternalMapper menuItemInternalMapper;

    @Transactional(readOnly = true)
    @Override
    public List<MenuItemSnapshotDto> getSnapshotsByIds(List<UUID> ids) {
        var uniqueIds = ids.stream().distinct().toList();

        List<MenuItem> found = menuItemRepository.findByIdIn(uniqueIds);
        Map<UUID, MenuItemSnapshotDto> byId = found.stream()
                .collect(Collectors.toMap(MenuItem::getId, menuItemInternalMapper::toSnapshot));

        // -- возвращаем в порядке запроса
        List<MenuItemSnapshotDto> result = new ArrayList<>(uniqueIds.size());
        for (UUID id : uniqueIds) {
            MenuItemSnapshotDto dto = byId.get(id);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }
}
