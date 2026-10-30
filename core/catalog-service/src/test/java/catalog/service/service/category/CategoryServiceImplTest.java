package catalog.service.service.category;

import catalog.service.dto.category.CategoryCreateDto;
import catalog.service.dto.category.CategoryDto;
import catalog.service.dto.category.CategoryUpdateDto;
import catalog.service.exception.ConflictException;
import catalog.service.exception.NotFoundException;
import catalog.service.mapper.category.CategoryMapper;
import catalog.service.model.MenuCategory;
import catalog.service.model.Restaurant;
import catalog.service.repository.CategoryRepository;
import catalog.service.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    RestaurantRepository restaurantRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock CategoryMapper categoryMapper;

    @InjectMocks
    CategoryServiceImpl service;

    @Test
    void createShouldThrowNotFoundWhenRestaurantMissing() {
        UUID restaurantId = UUID.randomUUID();
        CategoryCreateDto req = new CategoryCreateDto("Main", 0);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(restaurantId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Restaurant not found");

        verify(categoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void createShouldThrowConflictWhenNameExists() {
        UUID restaurantId = UUID.randomUUID();
        CategoryCreateDto req = new CategoryCreateDto("Main", 0);

        Restaurant r = mock(Restaurant.class);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(r));
        when(categoryRepository.existsByRestaurantIdAndNameIgnoreCase(restaurantId, "Main")).thenReturn(true);

        assertThatThrownBy(() -> service.create(restaurantId, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Category name already exists");

        verify(categoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void createShouldSaveAndReturnDtoWhenOk() {
        UUID restaurantId = UUID.randomUUID();

        CategoryCreateDto req = new CategoryCreateDto("Main", 0);

        Restaurant restaurant = mock(Restaurant.class);
        MenuCategory saved = mock(MenuCategory.class);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(categoryRepository.existsByRestaurantIdAndNameIgnoreCase(restaurantId, "Main")).thenReturn(false);
        when(categoryRepository.saveAndFlush(any(MenuCategory.class))).thenReturn(saved);

        CategoryDto expected = new CategoryDto(UUID.randomUUID(), restaurantId,
                "Main", 0, null, null);
        when(categoryMapper.toCategoryDto(saved)).thenReturn(expected);

        CategoryDto actual = service.create(restaurantId, req);

        assertThat(actual).isEqualTo(expected);

        verify(categoryRepository).saveAndFlush(any(MenuCategory.class));
        verify(categoryMapper).toCategoryDto(saved);
    }

    @Test
    void getAllShouldThrowNotFoundWhenRestaurantMissing() {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAll(restaurantId))
                .isInstanceOf(NotFoundException.class);

        verify(categoryRepository, never()).findAllByRestaurantId(any(), any());
    }

    @Test
    void getAllShouldCallRepositoryWithCorrectSortAndMap() {
        UUID restaurantId = UUID.randomUUID();

        Restaurant r = mock(Restaurant.class);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(r));

        MenuCategory c1 = mock(MenuCategory.class);
        MenuCategory c2 = mock(MenuCategory.class);

        when(categoryRepository.findAllByRestaurantId(eq(restaurantId), any(Sort.class)))
                .thenReturn(List.of(c1, c2));

        when(categoryMapper.toCategoryDto(c1))
                .thenReturn(new CategoryDto(UUID.randomUUID(), restaurantId, "A", 0, null, null));
        when(categoryMapper.toCategoryDto(c2))
                .thenReturn(new CategoryDto(UUID.randomUUID(), restaurantId, "B", 1, null, null));

        List<CategoryDto> result = service.getAll(restaurantId);

        assertThat(result).hasSize(2);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(categoryRepository).findAllByRestaurantId(eq(restaurantId), sortCaptor.capture());

        Sort sort = sortCaptor.getValue();
        assertThat(sort.getOrderFor("sortOrder")).isNotNull();
        assertThat(Objects.requireNonNull(sort.getOrderFor("sortOrder")).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(sort.getOrderFor("name")).isNotNull();
        assertThat(Objects.requireNonNull(sort.getOrderFor("name")).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void updateShouldThrowNotFoundWhenCategoryMissing() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new CategoryUpdateDto("X", 1)))
                .isInstanceOf(NotFoundException.class);

        verify(categoryRepository, never()).flush();
    }

    @Test
    void updateShouldThrowConflictWhenNameDuplicates() {
        UUID categoryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        MenuCategory c = mock(MenuCategory.class);
        Restaurant r = mock(Restaurant.class);

        when(c.getRestaurant()).thenReturn(r);
        when(r.getId()).thenReturn(restaurantId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(c));
        when(categoryRepository.existsByRestaurantIdAndNameIgnoreCaseAndIdNot(restaurantId, "Main", categoryId))
                .thenReturn(true);

        CategoryUpdateDto req = new CategoryUpdateDto("Main", null);

        assertThatThrownBy(() -> service.update(categoryId, req))
                .isInstanceOf(ConflictException.class);

        verify(c, never()).setName(anyString());
        verify(categoryRepository, never()).flush();
    }

    @Test
    void updateShouldUpdateFieldsFlushAndReturnDto() {
        UUID categoryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        MenuCategory c = mock(MenuCategory.class);
        Restaurant r = mock(Restaurant.class);

        when(c.getRestaurant()).thenReturn(r);
        when(r.getId()).thenReturn(restaurantId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(c));
        when(categoryRepository.existsByRestaurantIdAndNameIgnoreCaseAndIdNot(restaurantId, "New", categoryId))
                .thenReturn(false);

        CategoryUpdateDto req = new CategoryUpdateDto("New", 5);

        CategoryDto expected = new CategoryDto(categoryId, restaurantId, "New", 5, null, null);
        when(categoryMapper.toCategoryDto(c)).thenReturn(expected);

        CategoryDto actual = service.update(categoryId, req);

        assertThat(actual).isEqualTo(expected);

        verify(c).setName("New");
        verify(c).setSortOrder(5);
        verify(categoryRepository).flush();
        verify(categoryMapper).toCategoryDto(c);
    }

    @Test
    void deleteShouldThrowNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(NotFoundException.class);

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteShouldDeleteWhenFound() {
        UUID id = UUID.randomUUID();

        MenuCategory c = mock(MenuCategory.class);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(c));

        service.delete(id);

        verify(categoryRepository).delete(c);
    }
}