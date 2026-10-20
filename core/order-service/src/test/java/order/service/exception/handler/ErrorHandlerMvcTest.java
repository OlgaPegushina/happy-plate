package order.service.exception.handler;

import interaction.api.enums.OrderStatus;
import order.service.controller.OrderController;
import order.service.exception.ConflictException;
import order.service.exception.NotFoundException;
import order.service.service.OrderService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = OrderController.class,
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

    @Autowired
    MockMvc mvc;

    @MockitoBean
    OrderService service;

    @Test
    void shouldReturn400WhenValidationError() throws Exception {
        String badJson = """
                {
                  "customerId": null,
                  "restaurantId": "%s",
                  "items": []
                }
                """.formatted(UUID.randomUUID());

        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message", containsString("must")))
                .andExpect(jsonPath("$.message", containsString("must not be null")))
                .andExpect(jsonPath("$.message", containsString("must not be empty")));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenValidationError_onUpdateStatus() throws Exception {
        UUID orderId = UUID.randomUUID();

        String badJson = """
                {
                }
                """;

        mvc.perform(put("/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message", containsString("must")));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn404WhenNotFoundException() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(service.getById(orderId))
                .thenThrow(new NotFoundException("Order not found: " + orderId));

        mvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("Запрашиваемый ресурс не найден"))
                .andExpect(jsonPath("$.message").value("Order not found: " + orderId));

        verify(service).getById(orderId);
    }

    @Test
    void shouldReturn409WhenConflictException() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(service.updateStatus(eq(orderId), eq(OrderStatus.PAID)))
                .thenThrow(new ConflictException("Only CREATED order can be marked as PAID, current=DRAFT"));

        String okJson = """
                { "status": "PAID" }
                """;

        mvc.perform(put("/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(okJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.reason").value("Конфликт данных"))
                .andExpect(jsonPath("$.message")
                        .value("Only CREATED order can be marked as PAID, current=DRAFT"));

        verify(service).updateStatus(eq(orderId), eq(OrderStatus.PAID));
    }

    @Test
    void shouldReturn409WhenDataIntegrityViolation() throws Exception {
        UUID orderId = UUID.randomUUID();

        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(service).updateStatus(eq(orderId), eq(OrderStatus.CREATED));

        String okJson = """
                { "status": "CREATED" }
                """;

        mvc.perform(put("/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(okJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.reason").value("Конфликт ограничений"))
                .andExpect(jsonPath("$.message", containsString("FK violation")));

        verify(service).updateStatus(eq(orderId), eq(OrderStatus.CREATED));
    }

    @Test
    void shouldReturn500WhenUnhandledException() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(service.getById(orderId))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.reason").value("Ошибка сервера"))
                .andExpect(jsonPath("$.message").value("boom"));

        verify(service).getById(orderId);
    }
}