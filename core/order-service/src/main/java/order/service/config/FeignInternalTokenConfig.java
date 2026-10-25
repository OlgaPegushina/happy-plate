package order.service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static interaction.api.utility.AppConstants.HEADER;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class FeignInternalTokenConfig {

    @Bean
    public RequestInterceptor internalTokenInterceptor(
            @Value("${internal.token}") String token
    ) {
        return template -> {
            template.removeHeader(HEADER);
            template.header(HEADER, token);

            template.removeHeader("Authorization");
            template.removeHeader("authorization");
        };
    }
}