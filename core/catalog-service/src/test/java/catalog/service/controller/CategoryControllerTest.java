package catalog.service.controller;

import catalog.service.dto.category.CategoryCreateDto;
import catalog.service.dto.category.CategoryUpdateDto;
import catalog.service.service.category.CategoryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = CategoryController.class,
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
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean
    CategoryService service;

    @Test
    void createShouldReturn201AndPassDtoToService() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        String json = """
                {
                  "name": "Main",
                  "sortOrder": 0
                }
                """;

        when(service.create(eq(restaurantId), any(CategoryCreateDto.class)))
                .thenReturn(null);

        mvc.perform(post("/restaurants/{restaurantId}/categories", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        ArgumentCaptor<CategoryCreateDto> captor = ArgumentCaptor.forClass(CategoryCreateDto.class);
        verify(service).create(eq(restaurantId), captor.capture());

        assertThat(captor.getValue().name()).isEqualTo("Main");
        assertThat(captor.getValue().sortOrder()).isEqualTo(0);
    }

    @Test
    void createShouldReturn400WhenNameBlank() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        String json = """
                {
                  "name": "  ",
                  "sortOrder": 0
                }
                """;

        mvc.perform(post("/restaurants/{restaurantId}/categories", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createShouldReturn400WhenNameTooShort() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        String json = """
                {
                  "name": "A",
                  "sortOrder": 0
                }
                """;

        mvc.perform(post("/restaurants/{restaurantId}/categories", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createShouldReturn400WhenSortOrderNegative() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        String json = """
                {
                  "name": "Main",
                  "sortOrder": -1
                }
                """;

        mvc.perform(post("/restaurants/{restaurantId}/categories", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getAllShouldReturn200AndCallService() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        when(service.getAll(restaurantId)).thenReturn(List.of());

        mvc.perform(get("/restaurants/{restaurantId}/categories", restaurantId))
                .andExpect(status().isOk());

        verify(service).getAll(restaurantId);
    }

    @Test
    void updateShouldReturn200AndPassDtoToService() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                {
                  "name": "Updated",
                  "sortOrder": 10
                }
                """;

        when(service.update(eq(id), any(CategoryUpdateDto.class))).thenReturn(null);

        mvc.perform(put("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        ArgumentCaptor<CategoryUpdateDto> captor = ArgumentCaptor.forClass(CategoryUpdateDto.class);
        verify(service).update(eq(id), captor.capture());

        assertThat(captor.getValue().name()).isEqualTo("Updated");
        assertThat(captor.getValue().sortOrder()).isEqualTo(10);
    }

    @Test
    void updateShouldReturn400WhenSortOrderNegative() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                {
                  "sortOrder": -1
                }
                """;

        mvc.perform(put("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void deleteShouldReturn204AndCallService() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/categories/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}
