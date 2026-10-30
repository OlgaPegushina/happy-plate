package catalog.service.exception.handler;

import catalog.service.controller.CategoryController;
import catalog.service.exception.ConflictException;
import catalog.service.exception.NotFoundException;
import catalog.service.service.category.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
@Import(ErrorHandler.class)
class ErrorHandlerMvcTest {

    @Autowired MockMvc mvc;

    @MockitoBean
    CategoryService service;

    @Test
    void shouldReturn400WhenValidationError() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        String badJson = """
                {
                  "name": "  ",
                  "sortOrder": -1
                }
                """;

        mvc.perform(post("/restaurants/{restaurantId}/categories", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message", containsString("must")))
                .andExpect(jsonPath("$.message", containsString("greater than or equal to 0")));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn404WhenNotFoundException() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        when(service.getAll(restaurantId))
                .thenThrow(new NotFoundException("Restaurant not found: " + restaurantId));

        mvc.perform(get("/restaurants/{restaurantId}/categories", restaurantId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("Запрашиваемый ресурс не найден"))
                .andExpect(jsonPath("$.message").value("Restaurant not found: " + restaurantId));

        verify(service).getAll(restaurantId);
    }

    @Test
    void shouldReturn409WhenConflictException() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        when(service.create(eq(restaurantId), any()))
                .thenThrow(new ConflictException("Category name already exists"));

        String okJson = """
                {
                  "name": "Main",
                  "sortOrder": 0
                }
                """;

        mvc.perform(post("/restaurants/{restaurantId}/categories", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(okJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.reason").value("Конфликт данных"))
                .andExpect(jsonPath("$.message").value("Category name already exists"));

        verify(service).create(eq(restaurantId), any());
    }

    @Test
    void shouldReturn409WhenDataIntegrityViolation() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(service).delete(id);

        mvc.perform(delete("/categories/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.reason").value("Конфликт ограничений"))
                .andExpect(jsonPath("$.message", containsString("FK violation")));

        verify(service).delete(id);
    }

    @Test
    void shouldReturn500WhenUnhandledException() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        when(service.getAll(restaurantId))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/restaurants/{restaurantId}/categories", restaurantId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.reason").value("Ошибка сервера"))
                .andExpect(jsonPath("$.message").value("boom"));

        verify(service).getAll(restaurantId);
    }
}
