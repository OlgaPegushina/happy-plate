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
    private AppConstants() {
    }

    public static final String EXCHANGE_EVENTS = "food.events";

    public static final String RK_ORDER_CREATED = "order.created";
    public static final String RK_PAYMENT_PAID = "payment.paid";
    public static final String RK_DELIVERY_TAKEN = "delivery.taken";
    public static final String RK_DELIVERY_DELIVERED = "delivery.delivered";

    public static final String Q_NOTIFICATIONS = "notification.events.q";
    public static final String Q_DELIVERY_PAYMENT_PAID = "delivery.payment-paid.q";
    public static final String Q_PAYMENT_ORDER_CREATED = "payment.order-created.q";

    public static final String CLAIM_USER_ID = "user_id";
    public static final String CLAIM_ROLES = "roles";

    public static final String HEADER = "X-Internal-Token";

    public static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            DRAFT, Set.of(CREATED),
            CREATED, Set.of(PAID),
            PAID, Set.of(DELIVERY_TAKEN),
            DELIVERY_TAKEN, Set.of(DELIVERED),
            DELIVERED, Set.of()
    );
}
