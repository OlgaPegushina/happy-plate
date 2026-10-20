package catalog.service.mapper.category;

import catalog.service.model.MenuCategory;
import catalog.service.dto.category.CategoryDto;

public interface CategoryMapper {
    CategoryDto toCategoryDto(MenuCategory menuCategory);
}
