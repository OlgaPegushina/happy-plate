package catalog.service.controller;

import catalog.service.dto.menu.MenuItemCreateDto;
import catalog.service.dto.menu.MenuItemDto;
import catalog.service.dto.menu.MenuItemStatusUpdateDto;
import catalog.service.dto.menu.MenuItemUpdateDto;
import catalog.service.service.menu.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService service;

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemDto create(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuItemCreateDto req
    ) {
        return service.create(restaurantId, req);
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    @ResponseStatus(HttpStatus.OK)
    public List<MenuItemDto> getAll(
            @PathVariable UUID restaurantId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) UUID categoryId
    ) {
        return service.getAll(restaurantId, available, categoryId);
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemDto get(@PathVariable UUID restaurantId,
                           @PathVariable UUID id) {
        return service.get(restaurantId, id);
    }

    @PatchMapping("/restaurants/{restaurantId}/menu-items/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemDto update(@PathVariable UUID restaurantId,
                              @PathVariable UUID id,
                              @Valid @RequestBody MenuItemUpdateDto req) {
        return service.update(restaurantId, id, req);
    }

    @PatchMapping("/restaurants/{restaurantId}/menu-items/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemDto updateStatus(@PathVariable UUID restaurantId,
                                    @PathVariable UUID id,
                                    @Valid @RequestBody MenuItemStatusUpdateDto req) {
        return service.updateAvailableStatus(restaurantId, id, req.available());
    }

    @DeleteMapping("/restaurants/{restaurantId}/menu-items/{id}/category")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemDto clearCategory(@PathVariable UUID restaurantId,
                                     @PathVariable UUID id) {
        return service.clearCategory(restaurantId, id);
    }
}
