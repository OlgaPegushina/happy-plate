package order.service.controller;

import interaction.api.enums.OrderStatus;
import order.service.dto.order.OrderCreateDto;
import order.service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
@Import(OrderControllerTest.TestSecurityConfig.class)
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    OrderService orderService;

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
    void createShouldReturn201AndPassDtoToService() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID menuItemId = UUID.randomUUID();

        String json = """
                {
                  "restaurantId": "%s",
                  "items": [
                    { "menuItemId": "%s", "quantity": 2 }
                  ]
                }
                """.formatted(restaurantId, menuItemId);

        when(orderService.createDraft(any(OrderCreateDto.class))).thenReturn(null);

        mvc.perform(post("/orders")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        ArgumentCaptor<OrderCreateDto> captor = ArgumentCaptor.forClass(OrderCreateDto.class);
        verify(orderService).createDraft(captor.capture());

        assertThat(captor.getValue().restaurantId()).isEqualTo(restaurantId);
        assertThat(captor.getValue().items()).hasSize(1);
        assertThat(captor.getValue().items().getFirst().menuItemId()).isEqualTo(menuItemId);
        assertThat(captor.getValue().items().getFirst().quantity()).isEqualTo(2);
    }

    @Test
    void createShouldReturn401WhenNoJwt() throws Exception {
        String json = """
                {
                  "restaurantId": "%s",
                  "items": [ { "menuItemId": "%s", "quantity": 1 } ]
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void createShouldReturn400WhenRestaurantIdNull() throws Exception {
        String json = """
                {
                  "restaurantId": null,
                  "items": [ { "menuItemId": "%s", "quantity": 1 } ]
                }
                """.formatted(UUID.randomUUID());

        mvc.perform(post("/orders")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createShouldReturn400WhenItemsEmpty() throws Exception {
        String json = """
                {
                  "restaurantId": "%s",
                  "items": []
                }
                """.formatted(UUID.randomUUID());

        mvc.perform(post("/orders")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createShouldReturn400WhenItemQuantityNotPositive() throws Exception {
        String json = """
                {
                  "restaurantId": "%s",
                  "items": [ { "menuItemId": "%s", "quantity": 0 } ]
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mvc.perform(post("/orders")
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void getByIdShouldReturn200AndCallService() throws Exception {
        UUID id = UUID.randomUUID();

        when(orderService.getById(id)).thenReturn(null);

        mvc.perform(get("/orders/{id}", id)
                        .with(userJwt()))
                .andExpect(status().isOk());

        verify(orderService).getById(id);
    }

    @Test
    void updateStatusShouldReturn200AndPassEnumToService_whenAdmin() throws Exception {
        UUID id = UUID.randomUUID();

        when(orderService.adminUpdateStatus(id, OrderStatus.CREATED)).thenReturn(null);

        String json = """
                { "status": "CREATED" }
                """;

        mvc.perform(put("/orders/{id}/status", id)
                        .with(adminJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(orderService).adminUpdateStatus(id, OrderStatus.CREATED);
    }

    @Test
    void updateStatusShouldReturn403_whenNotAdmin() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                { "status": "CREATED" }
                """;

        mvc.perform(put("/orders/{id}/status", id)
                        .with(userJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    @Test
    void updateStatusShouldReturn400WhenStatusMissing() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                { }
                """;

        mvc.perform(put("/orders/{id}/status", id)
                        .with(adminJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void updateStatusShouldReturn400WhenStatusUnknown() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                { "status": "SOME_NEW_STATUS" }
                """;

        mvc.perform(put("/orders/{id}/status", id)
                        .with(adminJwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }
}