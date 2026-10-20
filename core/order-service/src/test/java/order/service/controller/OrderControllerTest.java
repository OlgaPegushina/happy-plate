package order.service.controller;

import interaction.api.enums.OrderStatus;
import order.service.dto.order.OrderCreateDto;
import order.service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    OrderService orderService;

    @Test
    void createShouldReturn201AndPassDtoToService() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID menuItemId = UUID.randomUUID();

        String json = """
                {
                  "customerId": "%s",
                  "restaurantId": "%s",
                  "items": [
                    {
                      "menuItemId": "%s",
                      "quantity": 2
                    }
                  ]
                }
                """.formatted(customerId, restaurantId, menuItemId);

        when(orderService.createDraft(any(OrderCreateDto.class)))
                .thenReturn(null);

        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        ArgumentCaptor<OrderCreateDto> captor = ArgumentCaptor.forClass(OrderCreateDto.class);
        verify(orderService).createDraft(captor.capture());

        assertThat(captor.getValue().customerId()).isEqualTo(customerId);
        assertThat(captor.getValue().restaurantId()).isEqualTo(restaurantId);
        assertThat(captor.getValue().items()).hasSize(1);
        assertThat(captor.getValue().items().getFirst().menuItemId()).isEqualTo(menuItemId);
        assertThat(captor.getValue().items().getFirst().quantity()).isEqualTo(2);
    }

    @Test
    void createShouldReturn400WhenCustomerIdNull() throws Exception {
        String json = """
                {
                  "customerId": null,
                  "restaurantId": "%s",
                  "items": [
                    { "menuItemId": "%s", "quantity": 1 }
                  ]
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createShouldReturn400WhenItemsEmpty() throws Exception {
        String json = """
                {
                  "customerId": "%s",
                  "restaurantId": "%s",
                  "items": []
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createShouldReturn400WhenItemQuantityNotPositive() throws Exception {
        String json = """
                {
                  "customerId": "%s",
                  "restaurantId": "%s",
                  "items": [
                    { "menuItemId": "%s", "quantity": 0 }
                  ]
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void getByIdShouldReturn200AndCallService() throws Exception {
        UUID id = UUID.randomUUID();

        when(orderService.getById(id)).thenReturn(null);

        mvc.perform(get("/orders/{id}", id))
                .andExpect(status().isOk());

        verify(orderService).getById(id);
    }

    @Test
    void updateStatusShouldReturn200AndPassEnumToService() throws Exception {
        UUID id = UUID.randomUUID();

        when(orderService.updateStatus(id, OrderStatus.CREATED)).thenReturn(null);

        String json = """
                { "status": "CREATED" }
                """;

        mvc.perform(put("/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(orderService).updateStatus(id, OrderStatus.CREATED);
    }

    @Test
    void updateStatusShouldReturn400WhenStatusMissing() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                { }
                """;

        mvc.perform(put("/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void updateStatusShouldReturn400WhenStatusUnknown() throws Exception {
        UUID id = UUID.randomUUID();

        // Jackson не сможет распарсить неизвестный enum - 400
        String json = """
                { "status": "SOME_NEW_STATUS" }
                """;

        mvc.perform(put("/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }
}
