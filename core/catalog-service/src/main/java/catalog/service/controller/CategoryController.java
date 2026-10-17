package catalog.service.controller;

import catalog.service.dto.category.CategoryCreateDto;
import catalog.service.dto.category.CategoryDto;
import catalog.service.dto.category.CategoryUpdateDto;
import catalog.service.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @PostMapping("/restaurants/{restaurantId}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CategoryCreateDto req
    ) {
        return service.create(restaurantId, req);
    }

    @GetMapping("/restaurants/{restaurantId}/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getAll(@PathVariable UUID restaurantId) {
        return service.getAll(restaurantId);
    }

    @PutMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(@PathVariable UUID id, @Valid @RequestBody CategoryUpdateDto req) {
        return service.update(id, req);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
