package notification.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationAmqpConfig {

    public static final String Q_NOTIFICATIONS = "notification.events.q";

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(Q_NOTIFICATIONS).build();
    }

    @Bean
    public Binding bindOrders(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("order.*");
    }

    @Bean
    public Binding bindPayments(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("payment.*");
    }

    @Bean
    public Binding bindDelivery(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("delivery.*");
    }
}