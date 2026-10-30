package payment.service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static interaction.api.utility.AppConstants.HEADER;

@Configuration
public class FeignInternalTokenConfig {

    @Bean
    public RequestInterceptor internalTokenInterceptor(
            @Value("${internal.token}") String token
    ) {
        return template -> template.header(HEADER, token);
    }
}