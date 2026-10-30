package catalog.service.controller.internal;

import interaction.api.dto.MenuItemIdsRequest;
import interaction.api.dto.MenuItemSnapshotDto;
import catalog.service.service.internal.MenuItemInternalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MenuItemInternalController {

    private final MenuItemInternalService menuItemInternalService;

    @PostMapping("/internal/menu-items/by-ids")
    public List<MenuItemSnapshotDto> getByIds(@RequestBody @Valid MenuItemIdsRequest req) {

        log.debug("[internal] by-ids start, ids={}", req.ids());

        var res = menuItemInternalService.getSnapshotsByIds(req.ids());

        log.debug("[internal] by-ids end, size={}", res.size());

        return res;
    }
}
