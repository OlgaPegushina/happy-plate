package interaction.api.utility;

import interaction.api.enums.OrderStatus;

import java.util.Map;
import java.util.Set;

import static interaction.api.enums.OrderStatus.CREATED;
import static interaction.api.enums.OrderStatus.DELIVERED;
import static interaction.api.enums.OrderStatus.DELIVERY_TAKEN;
import static interaction.api.enums.OrderStatus.DRAFT;
import static interaction.api.enums.OrderStatus.PAID;

public final class AppConstants {
    private AppConstants() {}

    /**
     * RabbitMQ exchange для доменных событий системы (event-driven интеграция).
     * Сюда публикуются события из order/payment/delivery, и отсюда их потребляют слушатели.
     */
    public static final String EXCHANGE_EVENTS = "food.events";

    /**
     * Routing key: заказ создан (order-service публикует после сохранения заказа).
     * Потребитель: payment-service (стартует обработку оплаты).
     */
    public static final String RK_ORDER_CREATED = "order.created";

    /**
     * Routing key: заказ оплачен (payment-service публикует после успешной "оплаты").
     * Потребитель: delivery-service (стартует доставку).
     */
    public static final String RK_PAYMENT_PAID = "payment.paid";

    /**
     * Routing key: доставка взята в работу (delivery-service публикует на промежуточном этапе).
     * Потребитель: notification-service (и любые другие реактивные обработчики в будущем).
     */
    public static final String RK_DELIVERY_TAKEN = "delivery.taken";

    /**
     * Routing key: доставка завершена (delivery-service публикует финальное событие).
     * Потребитель: notification-service (и любые другие реактивные обработчики в будущем).
     */
    public static final String RK_DELIVERY_DELIVERED = "delivery.delivered";

    /**
     * Очередь для notification-service: получает все доменные события для "уведомлений" (логирование).
     */
    public static final String Q_NOTIFICATIONS = "notification.events.q";

    /**
     * Очередь для delivery-service: получает событие "оплачено" (payment.paid),
     * после чего delivery-service эмулирует этапы доставки.
     */
    public static final String Q_DELIVERY_PAYMENT_PAID = "delivery.payment-paid.q";

    /**
     * Очередь для payment-service: получает событие "заказ создан" (order.created),
     * после чего payment-service эмулирует оплату.
     */
    public static final String Q_PAYMENT_ORDER_CREATED = "payment.order-created.q";

    /**
     * JWT claim: идентификатор пользователя (кладётся auth-service в access_token).
     * Используется сервисами для авторизации/аудита.
     */
    public static final String CLAIM_USER_ID = "user_id";

    /**
     * JWT claim: роли пользователя (например, USER/ADMIN).
     * Используется при проверке доступа к защищённым endpoint-ам.
     */
    public static final String CLAIM_ROLES = "roles";

    /**
     * Заголовок для внутренних запросов между сервисами (service-to-service).
     * Например: payment-service/delivery-service обновляют статус заказа в order-service.
     */
    public static final String HEADER = "X-Internal-Token";

    /**
     * Разрешённые переходы статусов заказа.
     * Используется order-service для валидации смены статуса.
     */
    public static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            DRAFT, Set.of(CREATED),
            CREATED, Set.of(PAID),
            PAID, Set.of(DELIVERY_TAKEN),
            DELIVERY_TAKEN, Set.of(DELIVERED),
            DELIVERED, Set.of()
    );
}
