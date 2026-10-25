package payment.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static interaction.api.utility.AppConstants.Q_PAYMENT_ORDER_CREATED;
import static interaction.api.utility.AppConstants.RK_ORDER_CREATED;

@Configuration
public class PaymentAmqpConfig {

    @Bean
    public Queue paymentOrderCreatedQueue() {
        return QueueBuilder.durable(Q_PAYMENT_ORDER_CREATED).build();
    }

    @Bean
    public Binding bindPaymentOrderCreated(Queue paymentOrderCreatedQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(paymentOrderCreatedQueue)
                .to(eventsExchange)
                .with(RK_ORDER_CREATED);
    }
}
