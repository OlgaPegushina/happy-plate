package catalog.service.mapper.category;

import catalog.service.model.MenuCategory;
import catalog.service.dto.category.CategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapperImpl implements CategoryMapper {
    @Override
    public CategoryDto toCategoryDto(MenuCategory menuCategory) {

        return new CategoryDto(
                menuCategory.getId(),
                menuCategory.getRestaurant().getId(),
                menuCategory.getName(),
                menuCategory.getSortOrder()
        );
    }
}
