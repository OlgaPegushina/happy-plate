package order.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/").description("Direct (8082)"))
                .addServersItem(new Server().url("/order-service").description("Via API Gateway"));
    }
}