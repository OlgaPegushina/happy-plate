package catalog.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/").description("Direct (8081)"))
                .addServersItem(new Server().url("/catalog-service").description("Via API Gateway"));
    }
}