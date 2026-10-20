CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE IF NOT EXISTS orders.orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    total_currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS orders.order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders.orders(id) ON DELETE CASCADE,

    menu_item_id UUID NOT NULL,
    name_snapshot VARCHAR(200) NOT NULL,

    unit_price_amount NUMERIC(19, 2) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL,

    quantity INT NOT NULL CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON orders.order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item_id ON orders.order_items(menu_item_id);