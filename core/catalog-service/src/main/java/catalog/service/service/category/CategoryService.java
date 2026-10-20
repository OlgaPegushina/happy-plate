package catalog.service.service.category;

import catalog.service.dto.category.CategoryCreateDto;
import catalog.service.dto.category.CategoryDto;
import catalog.service.dto.category.CategoryUpdateDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    @Transactional
    CategoryDto create(UUID restaurantId, CategoryCreateDto req);

    @Transactional(readOnly = true)
    List<CategoryDto> getAll(UUID restaurantId);

    @Transactional
    CategoryDto update(UUID id, CategoryUpdateDto req);

    @Transactional
    void delete(UUID id);
}
