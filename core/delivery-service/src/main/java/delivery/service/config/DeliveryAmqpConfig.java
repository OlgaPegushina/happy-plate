package delivery.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static interaction.api.utility.AppConstants.Q_DELIVERY_PAYMENT_PAID;
import static interaction.api.utility.AppConstants.RK_PAYMENT_PAID;

@Configuration
public class DeliveryAmqpConfig {

    @Bean
    public Queue deliveryPaymentPaidQueue() {
        return QueueBuilder.durable(Q_DELIVERY_PAYMENT_PAID).build();
    }

    @Bean
    public Binding bindDeliveryPaymentPaid(Queue deliveryPaymentPaidQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(deliveryPaymentPaidQueue)
                .to(eventsExchange)
                .with(RK_PAYMENT_PAID);
    }
}
