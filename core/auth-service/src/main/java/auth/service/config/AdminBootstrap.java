package auth.service.config;

import auth.service.exception.NotFoundException;
import auth.service.service.AdminInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminBootstrap {

    private final AdminInitializer adminInitializer;

    @Bean
    ApplicationRunner initAdmin(
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password
    ) {
        return args -> {
            try {
                adminInitializer.createAdminIfConfigured(email, password);
            } catch (NotFoundException e) {
                log.error("Admin bootstrap failed: required role missing. {}", e.getMessage());
            } catch (Exception e) {
                log.error("Failed to bootstrap admin user '{}': {}", email, e.getMessage(), e);
            }
        };
    }
}
