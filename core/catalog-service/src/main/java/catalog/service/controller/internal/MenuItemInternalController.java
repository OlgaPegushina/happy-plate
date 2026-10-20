package catalog.service.controller.internal;

import interaction.api.dto.MenuItemIdsRequest;
import interaction.api.dto.MenuItemSnapshotDto;
import catalog.service.service.internal.MenuItemInternalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuItemInternalController {

    private final MenuItemInternalService menuItemInternalService;

    @PostMapping("/internal/menu-items/by-ids")
    public List<MenuItemSnapshotDto> getByIds(@RequestBody @Valid MenuItemIdsRequest req) {
        return menuItemInternalService.getSnapshotsByIds(req.ids());
    }
}
