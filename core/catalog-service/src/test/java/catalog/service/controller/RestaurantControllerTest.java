package catalog.service.controller;

import catalog.service.dto.restaurant.RestaurantCreateDto;
import catalog.service.dto.restaurant.RestaurantUpdateDto;
import catalog.service.service.restaurant.RestaurantService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = RestaurantController.class,
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
class RestaurantControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean
    RestaurantService service;

    @Test
    void createShouldReturn201AndPassDtoToService() throws Exception {
        String json = """
                {
                  "name": "Pizza & Co",
                  "description": "Best pizza",
                  "phone": "+79991234567",
                  "address": "Moscow"
                }
                """;

        when(service.create(any(RestaurantCreateDto.class))).thenReturn(null);

        mvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        ArgumentCaptor<RestaurantCreateDto> captor = ArgumentCaptor.forClass(RestaurantCreateDto.class);
        verify(service).create(captor.capture());

        assertThat(captor.getValue().name()).isEqualTo("Pizza & Co");
        assertThat(captor.getValue().phone()).isEqualTo("+79991234567");
    }

    @Test
    void createShouldReturn400WhenPhoneInvalid() throws Exception {
        String json = """
                {
                  "name": "Pizza & Co",
                  "phone": "+123"
                }
                """;

        mvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getShouldReturn200AndCallService() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenReturn(null);

        mvc.perform(get("/restaurants/{id}", id))
                .andExpect(status().isOk());

        verify(service).get(id);
    }

    @Test
    void getAllShouldReturn200AndCallServiceWithNull() throws Exception {
        when(service.getAll(null)).thenReturn(List.of());

        mvc.perform(get("/restaurants"))
                .andExpect(status().isOk());

        verify(service).getAll(null);
    }

    @Test
    void getAllShouldReturn200AndPassActiveParam() throws Exception {
        when(service.getAll(true)).thenReturn(List.of());

        mvc.perform(get("/restaurants").param("active", "true"))
                .andExpect(status().isOk());

        verify(service).getAll(true);
    }

    @Test
    void updateShouldReturn200AndPassDtoToService() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                {
                  "description": "New desc",
                  "phone": "89991234567",
                  "address": "SPb"
                }
                """;

        when(service.update(eq(id), any(RestaurantUpdateDto.class))).thenReturn(null);

        mvc.perform(patch("/restaurants/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        ArgumentCaptor<RestaurantUpdateDto> captor = ArgumentCaptor.forClass(RestaurantUpdateDto.class);
        verify(service).update(eq(id), captor.capture());

        assertThat(captor.getValue().description()).isEqualTo("New desc");
        assertThat(captor.getValue().phone()).isEqualTo("89991234567");
        assertThat(captor.getValue().address()).isEqualTo("SPb");
    }

    @Test
    void updateShouldReturn400WhenPhoneInvalid() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                { "phone": "777" }
                """;

        mvc.perform(patch("/restaurants/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void updateStatusShouldReturn200AndCallService() throws Exception {
        UUID id = UUID.randomUUID();

        String json = """
                { "active": false }
                """;

        when(service.updateStatus(id, false)).thenReturn(null);

        mvc.perform(patch("/restaurants/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(service).updateStatus(id, false);
    }

    @Test
    void updateStatusShouldReturn400WhenActiveMissing() throws Exception {
        UUID id = UUID.randomUUID();

        String json = "{}";

        mvc.perform(patch("/restaurants/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
