package catalog.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CatalogServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApp.class, args);
    }
}
