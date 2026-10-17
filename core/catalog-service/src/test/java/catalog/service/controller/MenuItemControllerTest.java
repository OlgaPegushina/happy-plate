package catalog.service.controller;

import catalog.service.dto.menu.MenuItemCreateDto;
import catalog.service.dto.menu.MenuItemDto;
import catalog.service.dto.menu.MenuItemStatusUpdateDto;
import catalog.service.dto.menu.MenuItemUpdateDto;
import catalog.service.dto.menu.MoneyDto;
import catalog.service.service.menu.MenuItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = MenuItemController.class,
        properties = {
                "spring.config.import=",
                "spring.cloud.config.enabled=false",
                "spring.cloud.discovery.enabled=false",

                "eureka.client.enabled=false",
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false",

                "spring.autoconfigure.exclude=" +
                "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration," +
                "org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration"
        }
)
@AutoConfigureMockMvc(addFilters = false) // если есть Spring Security, чтобы фильтры не мешали
class MenuItemControllerTest {

    @Autowired MockMvc mvc;

    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    MenuItemService service;

    @Test
    void createShouldReturn201AndBody() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        MenuItemCreateDto req = new MenuItemCreateDto(
                "Latte",
                "Coffee",
                categoryId,
                new MoneyDto(new BigDecimal("5.50"), "USD"),
                10
        );

        MenuItemDto resp = new MenuItemDto(
                itemId,
                restaurantId,
                categoryId,
                "Latte",
                "Coffee",
                new MoneyDto(new BigDecimal("5.50"), "USD"),
                true,
                10,
                OffsetDateTime.parse("2026-10-17T10:00:00+03:00"),
                OffsetDateTime.parse("2026-10-17T10:00:00+03:00")
        );

        when(service.create(eq(restaurantId), any(MenuItemCreateDto.class))).thenReturn(resp);

        mvc.perform(post("/restaurants/{restaurantId}/menu-items", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Latte"))
                .andExpect(jsonPath("$.price.amount").value(5.50))
                .andExpect(jsonPath("$.price.currency").value("USD"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.sortOrder").value(10));

        ArgumentCaptor<MenuItemCreateDto> captor = ArgumentCaptor.forClass(MenuItemCreateDto.class);
        verify(service).create(eq(restaurantId), captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Latte");
        assertThat(captor.getValue().price().currency()).isEqualTo("USD");
    }

    @Test
    void getAllWithoutFiltersShouldReturn200() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        when(service.getAll(eq(restaurantId), isNull(), isNull()))
                .thenReturn(List.of());

        mvc.perform(get("/restaurants/{restaurantId}/menu-items", restaurantId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(service).getAll(restaurantId, null, null);
    }

    @Test
    void getAllWithFiltersShouldPassQueryParamsToService() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(service.getAll(eq(restaurantId), eq(true), eq(categoryId)))
                .thenReturn(List.of());

        mvc.perform(get("/restaurants/{restaurantId}/menu-items", restaurantId)
                        .param("available", "true")
                        .param("categoryId", categoryId.toString()))
                .andExpect(status().isOk());

        verify(service).getAll(restaurantId, true, categoryId);
    }

    @Test
    void getShouldReturn200() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        MenuItemDto resp = new MenuItemDto(
                itemId,
                restaurantId,
                null,
                "Burger",
                null,
                new MoneyDto(new BigDecimal("12.00"), "EUR"),
                true,
                0,
                OffsetDateTime.parse("2026-10-17T10:00:00+03:00"),
                OffsetDateTime.parse("2026-10-17T10:00:00+03:00")
        );

        when(service.get(restaurantId, itemId)).thenReturn(resp);

        mvc.perform(get("/restaurants/{restaurantId}/menu-items/{id}", restaurantId, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.name").value("Burger"));

        verify(service).get(restaurantId, itemId);
    }

    @Test
    void updateShouldReturn200() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        MenuItemUpdateDto req = new MenuItemUpdateDto(
                "New name",
                "New desc",
                null,
                new MoneyDto(new BigDecimal("7.00"), "USD"),
                3
        );

        MenuItemDto resp = new MenuItemDto(
                itemId,
                restaurantId,
                null,
                "New name",
                "New desc",
                new MoneyDto(new BigDecimal("7.00"), "USD"),
                true,
                3,
                OffsetDateTime.parse("2026-10-17T10:00:00+03:00"),
                OffsetDateTime.parse("2026-10-17T10:01:00+03:00")
        );

        when(service.update(eq(restaurantId), eq(itemId), any(MenuItemUpdateDto.class))).thenReturn(resp);

        mvc.perform(patch("/restaurants/{restaurantId}/menu-items/{id}", restaurantId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.sortOrder").value(3));

        verify(service).update(eq(restaurantId), eq(itemId), any(MenuItemUpdateDto.class));
    }

    @Test
    void updateShouldReturn400WhenNameTooShort() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        String badJson = """
                {"name":"A"}
                """;

        mvc.perform(patch("/restaurants/{restaurantId}/menu-items/{id}", restaurantId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void updateStatusShouldReturn200AndCallService() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        MenuItemDto resp = new MenuItemDto(
                itemId,
                restaurantId,
                null,
                "Item",
                null,
                new MoneyDto(new BigDecimal("1.00"), "USD"),
                false,
                0,
                OffsetDateTime.parse("2026-10-17T10:00:00+03:00"),
                OffsetDateTime.parse("2026-10-17T10:01:00+03:00")
        );

        when(service.updateAvailableStatus(restaurantId, itemId, false)).thenReturn(resp);

        MenuItemStatusUpdateDto req = new MenuItemStatusUpdateDto(false);

        mvc.perform(patch("/restaurants/{restaurantId}/menu-items/{id}/status", restaurantId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(service).updateAvailableStatus(restaurantId, itemId, false);
    }

    @Test
    void updateStatusShouldReturn400WhenAvailableIsNull() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        String badJson = "{}";

        mvc.perform(patch("/restaurants/{restaurantId}/menu-items/{id}/status", restaurantId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void clearCategoryShouldReturn200() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        MenuItemDto resp = new MenuItemDto(
                itemId,
                restaurantId,
                null,
                "Item",
                null,
                new MoneyDto(new BigDecimal("1.00"), "USD"),
                true,
                0,
                OffsetDateTime.parse("2026-10-17T10:00:00+03:00"),
                OffsetDateTime.parse("2026-10-17T10:01:00+03:00")
        );

        when(service.clearCategory(restaurantId, itemId)).thenReturn(resp);

        mvc.perform(delete("/restaurants/{restaurantId}/menu-items/{id}/category", restaurantId, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").doesNotExist()); // либо isEmpty(), зависит от сериализации

        verify(service).clearCategory(restaurantId, itemId);
    }
}
