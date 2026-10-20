package catalog.service.service.category;

import catalog.service.model.MenuCategory;
import catalog.service.model.Restaurant;
import catalog.service.dto.category.CategoryDto;
import catalog.service.dto.category.CategoryCreateDto;
import catalog.service.dto.category.CategoryUpdateDto;
import catalog.service.exception.ConflictException;
import catalog.service.exception.NotFoundException;
import catalog.service.mapper.category.CategoryMapper;
import catalog.service.repository.CategoryRepository;
import catalog.service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final RestaurantRepository restaurantRepository;

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CategoryDto create(UUID restaurantId, CategoryCreateDto req) {
        Restaurant restaurant = getRestaurant(restaurantId);

        if (categoryRepository.existsByRestaurantIdAndNameIgnoreCase(restaurantId, req.name())) {
            throw new ConflictException("Category name already exists for restaurant: " + req.name());
        }

        MenuCategory category = new MenuCategory(restaurant, req.name(), req.sortOrder());
        MenuCategory saved = categoryRepository.saveAndFlush(category);
        return categoryMapper.toCategoryDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getAll(UUID restaurantId) {
        getRestaurant(restaurantId);

        return categoryRepository.findAllByRestaurantId(
                        restaurantId,
                        Sort.by("sortOrder").ascending().and(Sort.by("name").ascending())
                )
                .stream().map(categoryMapper::toCategoryDto).toList();
    }

    @Transactional
    @Override
    public CategoryDto update(UUID id, CategoryUpdateDto req) {
        MenuCategory c = getCategory(id);

        if (req.name() != null) {
            UUID restaurantId = c.getRestaurant().getId();
            if (categoryRepository.existsByRestaurantIdAndNameIgnoreCaseAndIdNot(restaurantId, req.name(), id)) {
                throw new ConflictException("Category name already exists for restaurant: " + req.name());
            }
            c.setName(req.name());
        }

        if (req.sortOrder() != null) {
            c.setSortOrder(req.sortOrder());
        }

        categoryRepository.flush();

        return categoryMapper.toCategoryDto(c);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        MenuCategory c = getCategory(id);
        categoryRepository.delete(c);
    }

    private MenuCategory getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
    }

    private Restaurant getRestaurant(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantId));
    }
}
