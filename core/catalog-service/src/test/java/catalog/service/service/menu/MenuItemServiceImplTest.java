package catalog.service.service.menu;

import catalog.service.dto.menu.MenuItemCreateDto;
import catalog.service.dto.menu.MenuItemDto;
import catalog.service.dto.menu.MenuItemUpdateDto;
import catalog.service.dto.menu.MoneyDto;
import catalog.service.exception.NotFoundException;
import catalog.service.mapper.menu.MenuItemMapper;
import catalog.service.model.MenuCategory;
import catalog.service.model.MenuItem;
import catalog.service.model.Restaurant;
import catalog.service.repository.CategoryRepository;
import catalog.service.repository.MenuItemRepository;
import catalog.service.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceImplTest {

    @Mock RestaurantRepository restaurantRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock MenuItemRepository menuItemRepository;
    @Mock MenuItemMapper menuItemMapper;

    @InjectMocks MenuItemServiceImpl service;

    @Test
    void createShouldThrowNotFoundWhenRestaurantMissing() {
        UUID restaurantId = UUID.randomUUID();

        MenuItemCreateDto req = new MenuItemCreateDto(
                "Burger",
                "desc",
                null,
                new MoneyDto(new BigDecimal("10.00"), "RUB"),
                0
        );

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(restaurantId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Restaurant not found: " + restaurantId);

        verify(menuItemRepository, never()).saveAndFlush(any(MenuItem.class));
        verifyNoInteractions(categoryRepository, menuItemMapper);
    }

    @Test
    void createShouldThrowNotFoundWhenCategoryProvidedButMissing() {
        UUID restaurantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        MenuItemCreateDto req = new MenuItemCreateDto(
                "Burger",
                "desc",
                categoryId,
                new MoneyDto(new BigDecimal("10.00"), "RUB"),
                7
        );

        Restaurant restaurant = mock(Restaurant.class);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(categoryRepository.findByIdAndRestaurant_Id(categoryId, restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(restaurantId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found for restaurant: " + categoryId);

        verify(menuItemRepository, never()).saveAndFlush(any(MenuItem.class));
        verifyNoInteractions(menuItemMapper);
    }

    @Test
    void createShouldNotCheckCategoryWhenCategoryIdNullAndSave() {
        UUID restaurantId = UUID.randomUUID();

        MenuItemCreateDto req = new MenuItemCreateDto(
                "Burger",
                "desc",
                null,
                new MoneyDto(new BigDecimal("10.00"), "RUB"),
                7
        );

        Restaurant restaurant = mock(Restaurant.class);
        MenuItem saved = mock(MenuItem.class);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.saveAndFlush(any(MenuItem.class))).thenReturn(saved);

        MenuItemDto expected = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(saved)).thenReturn(expected);

        MenuItemDto actual = service.create(restaurantId, req);

        assertThat(actual).isSameAs(expected);

        verify(categoryRepository, never()).findByIdAndRestaurant_Id(any(UUID.class), any(UUID.class));
        verify(menuItemRepository).saveAndFlush(any(MenuItem.class));
        verify(menuItemMapper).toMenuItemDto(saved);
    }

    @Test
    void createShouldCheckCategoryWhenCategoryIdProvidedAndSave() {
        UUID restaurantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        MenuItemCreateDto req = new MenuItemCreateDto(
                "Burger",
                "desc",
                categoryId,
                new MoneyDto(new BigDecimal("10.00"), "RUB"),
                7
        );

        Restaurant restaurant = mock(Restaurant.class);
        MenuCategory category = mock(MenuCategory.class);
        MenuItem saved = mock(MenuItem.class);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(categoryRepository.findByIdAndRestaurant_Id(categoryId, restaurantId)).thenReturn(Optional.of(category));
        when(menuItemRepository.saveAndFlush(any(MenuItem.class))).thenReturn(saved);

        MenuItemDto expected = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(saved)).thenReturn(expected);

        MenuItemDto actual = service.create(restaurantId, req);

        assertThat(actual).isSameAs(expected);

        verify(categoryRepository).findByIdAndRestaurant_Id(categoryId, restaurantId);
        verify(menuItemRepository).saveAndFlush(any(MenuItem.class));
        verify(menuItemMapper).toMenuItemDto(saved);
    }

    @Test
    void getShouldThrowNotFoundWhenMissing() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(restaurantId, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Menu item not found: " + id);

        verifyNoInteractions(menuItemMapper);
    }

    @Test
    void getShouldReturnMappedDtoWhenFound() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        MenuItem item = mock(MenuItem.class);
        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.of(item));

        MenuItemDto expected = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(item)).thenReturn(expected);

        MenuItemDto actual = service.get(restaurantId, id);

        assertThat(actual).isSameAs(expected);
        verify(menuItemMapper).toMenuItemDto(item);
    }

    @Test
    void getAllSWhouldThrowNotFoundWhenRestaurantMissing() {
        UUID restaurantId = UUID.randomUUID();

        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        assertThatThrownBy(() -> service.getAll(restaurantId, null, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Restaurant not found: " + restaurantId);

        verify(menuItemRepository, never()).findForRestaurant(any(UUID.class), any(), any());
        verifyNoInteractions(categoryRepository, menuItemMapper);
    }

    @Test
    void getAllShouldThrowNotFoundWhenCategoryNotBelongsToRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(categoryRepository.findByIdAndRestaurant_Id(categoryId, restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAll(restaurantId, null, categoryId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found for restaurant: " + categoryId);

        verify(menuItemRepository, never()).findForRestaurant(any(UUID.class), any(), any());
        verifyNoInteractions(menuItemMapper);
    }

    @Test
    void getAllShouldReturnListWhenOkWithoutCategoryFilter() {
        UUID restaurantId = UUID.randomUUID();
        Boolean available = true;

        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);

        MenuItem i1 = mock(MenuItem.class);
        MenuItem i2 = mock(MenuItem.class);

        when(menuItemRepository.findForRestaurant(restaurantId, available, null))
                .thenReturn(List.of(i1, i2));

        MenuItemDto d1 = mock(MenuItemDto.class);
        MenuItemDto d2 = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(i1)).thenReturn(d1);
        when(menuItemMapper.toMenuItemDto(i2)).thenReturn(d2);

        List<MenuItemDto> result = service.getAll(restaurantId, available, null);

        assertThat(result).containsExactly(d1, d2);

        verify(menuItemRepository).findForRestaurant(restaurantId, available, null);
        verify(menuItemMapper).toMenuItemDto(i1);
        verify(menuItemMapper).toMenuItemDto(i2);
        verify(categoryRepository, never()).findByIdAndRestaurant_Id(any(UUID.class), any(UUID.class));
    }

    @Test
    void getAllShouldReturnListWhenOkWithCategoryFilter() {
        UUID restaurantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Boolean available = null;

        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);

        MenuCategory category = mock(MenuCategory.class);
        when(categoryRepository.findByIdAndRestaurant_Id(categoryId, restaurantId)).thenReturn(Optional.of(category));

        MenuItem i1 = mock(MenuItem.class);
        when(menuItemRepository.findForRestaurant(restaurantId, available, categoryId))
                .thenReturn(List.of(i1));

        MenuItemDto d1 = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(i1)).thenReturn(d1);

        List<MenuItemDto> result = service.getAll(restaurantId, available, categoryId);

        assertThat(result).containsExactly(d1);

        verify(menuItemRepository).findForRestaurant(restaurantId, available, categoryId);
        verify(menuItemMapper).toMenuItemDto(i1);
    }

    @Test
    void updateShouldThrowNotFoundWhenItemMissing() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        MenuItemUpdateDto req = new MenuItemUpdateDto(
                "New",
                "New desc",
                null,
                null,
                10
        );

        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(restaurantId, id, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Menu item not found: " + id);

        verify(menuItemRepository, never()).flush();
        verifyNoInteractions(categoryRepository, menuItemMapper);
    }

    @Test
    void updateShouldUpdateOnlyNonNullFieldsFlushAndReturnDto() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        MenuItem item = mock(MenuItem.class);
        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.of(item));

        MenuItemUpdateDto req = new MenuItemUpdateDto(
                null,
                "New desc",
                null,
                null,
                null
        );

        MenuItemDto expected = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(item)).thenReturn(expected);

        MenuItemDto actual = service.update(restaurantId, id, req);

        assertThat(actual).isSameAs(expected);

        verify(item, never()).setName(any());
        verify(item).setDescription("New desc");

        verify(item, never()).setCategory(any(MenuCategory.class));
        verify(item, never()).setSortOrder(anyInt());   // ключевая правка
        verify(item, never()).setPrice(any());

        verify(menuItemRepository).flush();
        verify(menuItemMapper).toMenuItemDto(item);

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void updateShouldThrowNotFoundWhenNewCategoryMissingForRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        MenuItem item = mock(MenuItem.class);
        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.of(item));

        MenuItemUpdateDto req = new MenuItemUpdateDto(
                null,
                null,
                newCategoryId,
                null,
                null
        );

        when(categoryRepository.findByIdAndRestaurant_Id(newCategoryId, restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(restaurantId, id, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found for restaurant: " + newCategoryId);

        verify(menuItemRepository, never()).flush();
        verifyNoInteractions(menuItemMapper);
    }

    @Test
    void updateShouldUpdateProvidedFieldsIncludingCategoryAndPriceAndSortOrder() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        MenuItem item = mock(MenuItem.class);
        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.of(item));

        MenuCategory category = mock(MenuCategory.class);
        when(categoryRepository.findByIdAndRestaurant_Id(newCategoryId, restaurantId)).thenReturn(Optional.of(category));

        MenuItemUpdateDto req = new MenuItemUpdateDto(
                "New name",
                "New desc",
                newCategoryId,
                new MoneyDto(new BigDecimal("12.50"), "RUB"),
                99
        );

        MenuItemDto expected = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(item)).thenReturn(expected);

        MenuItemDto actual = service.update(restaurantId, id, req);

        assertThat(actual).isSameAs(expected);

        verify(item).setName("New name");
        verify(item).setDescription("New desc");
        verify(item).setCategory(category);
        verify(item).setSortOrder(99);
        verify(item).setPrice(any());

        verify(menuItemRepository).flush();
        verify(menuItemMapper).toMenuItemDto(item);
    }

    @Test
    void updateAvailableStatusShouldThrowNotFoundWhenMissing() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAvailableStatus(restaurantId, id, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Menu item not found: " + id);

        verify(menuItemRepository, never()).flush();
        verifyNoInteractions(menuItemMapper);
    }

    @Test
    void updateAvailableStatusShouldSetAvailableFlushAndReturnDto() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        MenuItem item = mock(MenuItem.class);
        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.of(item));

        MenuItemDto expected = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(item)).thenReturn(expected);

        MenuItemDto actual = service.updateAvailableStatus(restaurantId, id, false);

        assertThat(actual).isSameAs(expected);

        verify(item).setAvailable(false);
        verify(menuItemRepository).flush();
        verify(menuItemMapper).toMenuItemDto(item);
    }

    @Test
    void clearCategoryShouldThrowNotFoundWhenMissing() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.clearCategory(restaurantId, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Menu item not found: " + id);

        verify(menuItemRepository, never()).flush();
        verifyNoInteractions(menuItemMapper);
    }

    @Test
    void clearCategoryShouldSetNullFlushAndReturnDto() {
        UUID restaurantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        MenuItem item = mock(MenuItem.class);
        when(menuItemRepository.findByIdAndRestaurant_Id(id, restaurantId)).thenReturn(Optional.of(item));

        MenuItemDto expected = mock(MenuItemDto.class);
        when(menuItemMapper.toMenuItemDto(item)).thenReturn(expected);

        MenuItemDto actual = service.clearCategory(restaurantId, id);

        assertThat(actual).isSameAs(expected);

        verify(item).setCategory(null);
        verify(menuItemRepository).flush();
        verify(menuItemMapper).toMenuItemDto(item);
    }
}