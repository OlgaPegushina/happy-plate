package catalog.service.service.restaurant;

import catalog.service.dto.restaurant.RestaurantCreateDto;
import catalog.service.dto.restaurant.RestaurantDto;
import catalog.service.dto.restaurant.RestaurantUpdateDto;
import catalog.service.exception.ConflictException;
import catalog.service.exception.NotFoundException;
import catalog.service.mapper.restaurant.RestaurantMapper;
import catalog.service.model.Restaurant;
import catalog.service.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock RestaurantRepository restaurantRepository;

    @Mock RestaurantMapper restaurantMapper;

    @InjectMocks RestaurantServiceImpl service;

    @Test
    void createShouldThrowConflictWhenNameExists() {
        RestaurantCreateDto req = new RestaurantCreateDto(
                "Pizza & Co", "desc", "+79991234567", "Moscow"
        );

        when(restaurantRepository.existsByNameIgnoreCase("Pizza & Co")).thenReturn(true);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Restaurant name already exists: Pizza & Co");

        verify(restaurantRepository, never()).saveAndFlush(any());
        verifyNoInteractions(restaurantMapper);
    }

    @Test
    void createShouldSaveAndReturnDtoWhenOk() {
        RestaurantCreateDto req = new RestaurantCreateDto(
                "Pizza & Co", "desc", "+79991234567", "Moscow"
        );

        when(restaurantRepository.existsByNameIgnoreCase("Pizza & Co")).thenReturn(false);

        Restaurant saved = mock(Restaurant.class);
        when(restaurantRepository.saveAndFlush(any(Restaurant.class))).thenReturn(saved);

        RestaurantDto expected = mock(RestaurantDto.class);
        when(restaurantMapper.toRestaurantDto(saved)).thenReturn(expected);

        RestaurantDto actual = service.create(req);

        assertThat(actual).isSameAs(expected);

        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepository).saveAndFlush(captor.capture());

        Restaurant toSave = captor.getValue();
        assertThat(toSave.getName()).isEqualTo("Pizza & Co");
        assertThat(toSave.getDescription()).isEqualTo("desc");
        assertThat(toSave.getPhone()).isEqualTo("+79991234567");
        assertThat(toSave.getAddress()).isEqualTo("Moscow");

        verify(restaurantMapper).toRestaurantDto(saved);
    }

    @Test
    void getShouldThrowNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Restaurant not found: " + id);

        verifyNoInteractions(restaurantMapper);
    }

    @Test
    void getShouldReturnMappedDtoWhenFound() {
        UUID id = UUID.randomUUID();

        Restaurant restaurant = mock(Restaurant.class);
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));

        RestaurantDto expected = mock(RestaurantDto.class);
        when(restaurantMapper.toRestaurantDto(restaurant)).thenReturn(expected);

        RestaurantDto actual = service.get(id);

        assertThat(actual).isSameAs(expected);
        verify(restaurantMapper).toRestaurantDto(restaurant);
    }

    @Test
    void getAllShouldUseFindAllWhenActiveNullAndSortByNameAsc() {
        Restaurant r1 = mock(Restaurant.class);
        Restaurant r2 = mock(Restaurant.class);

        when(restaurantRepository.findAll(any(Sort.class))).thenReturn(List.of(r1, r2));

        RestaurantDto d1 = mock(RestaurantDto.class);
        RestaurantDto d2 = mock(RestaurantDto.class);
        when(restaurantMapper.toRestaurantDto(r1)).thenReturn(d1);
        when(restaurantMapper.toRestaurantDto(r2)).thenReturn(d2);

        List<RestaurantDto> result = service.getAll(null);

        assertThat(result).containsExactly(d1, d2);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(restaurantRepository).findAll(sortCaptor.capture());

        Sort sort = sortCaptor.getValue();
        assertThat(sort.getOrderFor("name")).isNotNull();
        assertThat(sort.getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);

        verify(restaurantRepository, never()).findAllByActive(anyBoolean(), any(Sort.class));
    }

    @Test
    void getAllShouldUseFindAllByActiveWhenActiveProvidedAndSortByNameAsc() {
        boolean active = true;

        Restaurant r1 = mock(Restaurant.class);
        when(restaurantRepository.findAllByActive(eq(active), any(Sort.class)))
                .thenReturn(List.of(r1));

        RestaurantDto d1 = mock(RestaurantDto.class);
        when(restaurantMapper.toRestaurantDto(r1)).thenReturn(d1);

        List<RestaurantDto> result = service.getAll(active);

        assertThat(result).containsExactly(d1);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(restaurantRepository).findAllByActive(eq(active), sortCaptor.capture());

        Sort sort = sortCaptor.getValue();
        assertThat(sort.getOrderFor("name")).isNotNull();
        assertThat(sort.getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);

        verify(restaurantRepository, never()).findAll(any(Sort.class));
    }

    @Test
    void updateShouldThrowNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        RestaurantUpdateDto req = new RestaurantUpdateDto("desc", "+79991234567", "addr");

        assertThatThrownBy(() -> service.update(id, req))
                .isInstanceOf(NotFoundException.class);

        verify(restaurantRepository, never()).flush();
        verifyNoInteractions(restaurantMapper);
    }

    @Test
    void updateShouldUpdateOnlyNonNullFieldsFlushAndReturnDto() {
        UUID id = UUID.randomUUID();

        Restaurant restaurant = mock(Restaurant.class);
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));

        // -- хотим обновить только phone
        RestaurantUpdateDto req = new RestaurantUpdateDto(null, "+79990001122", null);

        RestaurantDto expected = mock(RestaurantDto.class);
        when(restaurantMapper.toRestaurantDto(restaurant)).thenReturn(expected);

        RestaurantDto actual = service.update(id, req);

        assertThat(actual).isSameAs(expected);

        verify(restaurant, never()).setDescription(any());
        verify(restaurant).setPhone("+79990001122");
        verify(restaurant, never()).setAddress(any());

        verify(restaurantRepository).flush();
        verify(restaurantMapper).toRestaurantDto(restaurant);
    }

    @Test
    void updateStatusShouldThrowNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(id, true))
                .isInstanceOf(NotFoundException.class);

        verify(restaurantRepository, never()).flush();
        verifyNoInteractions(restaurantMapper);
    }

    @Test
    void updateStatusShouldSetActiveFlushAndReturnDto() {
        UUID id = UUID.randomUUID();

        Restaurant restaurant = mock(Restaurant.class);
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));

        RestaurantDto expected = mock(RestaurantDto.class);
        when(restaurantMapper.toRestaurantDto(restaurant)).thenReturn(expected);

        RestaurantDto actual = service.updateStatus(id, false);

        assertThat(actual).isSameAs(expected);

        verify(restaurant).setActive(false);
        verify(restaurantRepository).flush();
        verify(restaurantMapper).toRestaurantDto(restaurant);
    }
}
