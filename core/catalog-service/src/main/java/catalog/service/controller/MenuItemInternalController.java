package catalog.service.controller;

import catalog.service.dto.internal.MenuItemIdsRequest;
import catalog.service.dto.internal.MenuItemSnapshotDto;
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
