package catalog.service.controller;

import catalog.service.dto.restaurant.RestaurantCreateDto;
import catalog.service.dto.restaurant.RestaurantDto;
import catalog.service.dto.restaurant.RestaurantStatusUpdateDto;
import catalog.service.dto.restaurant.RestaurantUpdateDto;
import catalog.service.service.restaurant.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class RestaurantController {

    private final RestaurantService service;

    @PostMapping("/restaurants")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantDto create(@Valid @RequestBody RestaurantCreateDto req) {
        return service.create(req);
    }

    @GetMapping("/restaurants/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantDto get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping("/restaurants")
    @ResponseStatus(HttpStatus.OK)
    public List<RestaurantDto> getAll(@RequestParam(required = false) Boolean active) {
        return service.getAll(active);
    }

    @PatchMapping("/restaurants/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantDto update(@PathVariable UUID id, @Valid @RequestBody RestaurantUpdateDto req) {
        return service.update(id, req);
    }

    @PatchMapping("/restaurants/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantDto updateStatus(@PathVariable UUID id,
                                      @Valid @RequestBody RestaurantStatusUpdateDto req) {
        return service.updateStatus(id, req.active());
    }
}
