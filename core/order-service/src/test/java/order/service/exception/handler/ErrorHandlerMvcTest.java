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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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
@AutoConfigureMockMvc
@Import({
        ErrorHandler.class,
        ErrorHandlerMvcTest.TestSecurityConfig.class
})
class ErrorHandlerMvcTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    OrderService service;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .authorizeHttpRequests(a -> a
                            .requestMatchers(HttpMethod.PUT, "/orders/*/status").hasRole("ADMIN")
                            .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                    .build();
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-user")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }
    }

    private static RequestPostProcessor userJwt() {
        return jwt().authorities(() -> "ROLE_USER");
    }

    private static RequestPostProcessor adminJwt() {
        return jwt().authorities(() -> "ROLE_ADMIN");
    }

    @Test
    void shouldReturn400WhenValidationError() throws Exception {
        String badJson = """
                {
                  "restaurantId": null,
                  "items": []
                }
                """;

        mvc.perform(post("/orders")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message", containsString("RestaurantId must not be null")))
                .andExpect(jsonPath("$.message", containsString("Items must not be empty")));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenValidationError_onUpdateStatus() throws Exception {
        UUID orderId = UUID.randomUUID();
        String badJson = "{}";

        mvc.perform(put("/orders/{id}/status", orderId)
                        .with(adminJwt())   // иначе будет 403 по правилу в цепочке
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message").value("must not be null"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn404WhenNotFoundException() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(service.getById(eq(orderId)))
                .thenThrow(new NotFoundException("Order not found: " + orderId));

        mvc.perform(get("/orders/{id}", orderId)
                        .with(userJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("Запрашиваемый ресурс не найден"))
                .andExpect(jsonPath("$.message").value("Order not found: " + orderId));

        verify(service).getById(eq(orderId));
    }

    @Test
    void shouldReturn409WhenConflictException() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(service.adminUpdateStatus(eq(orderId), eq(OrderStatus.PAID)))
                .thenThrow(new ConflictException("Only CREATED order can be marked as PAID, current=DRAFT"));

        String okJson = """
                { "status": "PAID" }
                """;

        mvc.perform(put("/orders/{id}/status", orderId)
                        .with(adminJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(okJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.reason").value("Конфликт данных"))
                .andExpect(jsonPath("$.message")
                        .value("Only CREATED order can be marked as PAID, current=DRAFT"));

        verify(service).adminUpdateStatus(eq(orderId), eq(OrderStatus.PAID));
    }

    @Test
    void shouldReturn409WhenDataIntegrityViolation() throws Exception {
        UUID orderId = UUID.randomUUID();

        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(service).adminUpdateStatus(eq(orderId), eq(OrderStatus.CREATED));

        String okJson = """
                { "status": "CREATED" }
                """;

        mvc.perform(put("/orders/{id}/status", orderId)
                        .with(adminJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(okJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.reason").value("Конфликт ограничений"))
                .andExpect(jsonPath("$.message", containsString("FK violation")));

        verify(service).adminUpdateStatus(eq(orderId), eq(OrderStatus.CREATED));
    }

    @Test
    void shouldReturn500WhenUnhandledException() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(service.getById(eq(orderId)))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/orders/{id}", orderId)
                        .with(userJwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.reason").value("Ошибка сервера"))
                .andExpect(jsonPath("$.message").value("boom"));

        verify(service).getById(eq(orderId));
    }
}