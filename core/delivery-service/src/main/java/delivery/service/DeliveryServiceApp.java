package delivery.service;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
@EnableFeignClients
public class DeliveryServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceApp.class, args);
    }
}