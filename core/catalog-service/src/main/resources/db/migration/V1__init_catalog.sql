-- Создание расширения и схемы
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE SCHEMA IF NOT EXISTS catalog;

-- Restaurants
CREATE TABLE catalog.restaurants (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name         varchar(200) NOT NULL,
  description  varchar(1000),
  phone        varchar(12),
  address      varchar(500),
  active       boolean NOT NULL DEFAULT true,
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_catalog_restaurants_name_ci
  ON catalog.restaurants (lower(name));

-- Menu Categories
CREATE TABLE catalog.menu_categories (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  restaurant_id  uuid NOT NULL,
  name           varchar(120) NOT NULL,
  sort_order     int NOT NULL DEFAULT 0,
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT fk_menu_categories_restaurant
    FOREIGN KEY (restaurant_id)
    REFERENCES catalog.restaurants(id)
    ON DELETE CASCADE
);

-- Быстрый поиск по restaurant_id
CREATE INDEX IF NOT EXISTS ix_menu_categories_restaurant_id
  ON catalog.menu_categories (restaurant_id);

CREATE INDEX IF NOT EXISTS ix_menu_categories_sort
  ON catalog.menu_categories (restaurant_id, sort_order);

-- Уникальность имени категории внутри ресторана без учёта регистра:
-- (restaurant_id, lower(name))
CREATE UNIQUE INDEX IF NOT EXISTS ux_menu_categories_restaurant_name_ci
  ON catalog.menu_categories (restaurant_id, lower(name));

-- Menu Items
CREATE TABLE catalog.menu_items (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  restaurant_id  uuid NOT NULL REFERENCES catalog.restaurants(id) ON DELETE CASCADE,
  category_id    uuid REFERENCES catalog.menu_categories(id) ON DELETE SET NULL,

  name           varchar(200) NOT NULL,
  description    varchar(2000),

  price_amount   numeric(10,2) NOT NULL,
  price_currency varchar(3) NOT NULL,

  available      boolean NOT NULL DEFAULT true,
  sort_order     int NOT NULL DEFAULT 0,

  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now(),

  -- ограничения
  CONSTRAINT chk_menu_items_price_amount_non_negative CHECK (price_amount >= 0),
  CONSTRAINT chk_menu_items_currency_format CHECK (
    price_currency ~ '^[A-Z]{3}$'
  )
);

CREATE INDEX IF NOT EXISTS ix_menu_items_restaurant_id
  ON catalog.menu_items (restaurant_id);

CREATE INDEX IF NOT EXISTS ix_menu_items_restaurant_available
  ON catalog.menu_items (restaurant_id, available);

CREATE INDEX IF NOT EXISTS ix_menu_items_category_id
  ON catalog.menu_items (category_id);

-- Комментарии для документации
COMMENT ON TABLE catalog.restaurants IS 'Рестораны в системе доставки';
COMMENT ON TABLE catalog.menu_categories IS 'Категории меню ресторанов';
COMMENT ON TABLE catalog.menu_items IS 'Позиции меню ресторанов';

COMMENT ON COLUMN catalog.menu_items.price_currency IS 'Валюта в формате ISO 4217 (RUB, USD, EUR)';
COMMENT ON COLUMN catalog.menu_items.available IS 'Доступность позиции для заказа';