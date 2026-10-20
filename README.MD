# HappyPlate — микросервисная платформа доставки еды

(Java 21 и Spring Boot 3.5.x.)

Фокус проекта:
- инфраструктурные компоненты (Gateway, Discovery, Config)
- синхронные вызовы между сервисами через OpenFeign
- асинхронное взаимодействие через RabbitMQ (доменные события)
- наблюдаемость (Actuator + Zipkin tracing)
- запуск целиком через Docker Compose

---

## 1) Состав системы

### Карта сервисов (порты)

| Компонент |  Порт | Назначение |
|---|------:|---|
| `gateway-server` |  8080 | Единая точка входа, маршрутизация запросов |
| `catalog-service` |  8081 | Каталог ресторанов и меню |
| `order-service` |  8082 | Заказы: создание, статусы, “снимок” заказа |
| `payment-service` |  8084 | Заглушка оплаты (stateless), реакция на события |
| `delivery-service` |  8083 | Заглушка доставки (stateless), реакция на события |
| `notification-service` |  8085 | Заглушка уведомлений (stateless), реакция на события |
| `discovery-server` (Eureka) |  8761 | Реестр сервисов |
| `config-server` |  8888 | Централизованные конфиги (native) |
| `zipkin` |  9411 | Distributed tracing |
| `rabbitmq` (AMQP) |  5672 | Брокер сообщений |
| `rabbitmq` (UI) | 15672 | RabbitMQ Management (guest/guest) |
| `postgres` |  5432 | База данных dev-окружения |

---

## 2) Ответственность сервисов

### Инфраструктура
- `discovery-server` (Eureka)
  - сервисы регистрируются и находят друг друга по имени
- `config-server` (Spring Cloud Config, native)
  - централизованные конфиги из файловой системы
- `gateway-server` (Spring Cloud Gateway)
  - маршрутизация внешних запросов к backend-сервисам
- `zipkin`
  - сбор распределённых трасс
- `rabbitmq`
  - транспорт доменных событий
- `postgres`
  - единый контейнер PostgreSQL для dev
  - миграции через Flyway

### Бизнес-сервисы
- `catalog-service`
  - хранит и отдаёт каталог (рестораны, категории, позиции меню)
  - CRUD/API для управления каталогом
- `order-service`
  - создаёт заказ и управляет статусом заказа
  - хранит “снимок” заказа на момент покупки (денормализация: название/цена на момент заказа)
  - публикует событие `order.created`
- `payment-service` (заглушка, stateless)
  - получает `order.created`
  - эмулирует успешную оплату
  - обновляет статус заказа в `order-service` через Feign
  - публикует событие `payment.paid`
- `delivery-service` (заглушка, stateless)
  - получает `payment.paid`
  - эмулирует доставку (этапы)
  - обновляет статус заказа в `order-service` через Feign
  - публикует события `delivery.taken` и `delivery.delivered`
- `notification-service` (заглушка, stateless)
  - получает доменные события
  - эмулирует уведомления (например, запись в лог)
  - обновляет статус заказа в `order-service` через Feign (если предусмотрено контрактом)

---

## 3) Почему микросервисы (Design Decisions)

- Независимое развитие и масштабирование:
  - `catalog-service` — много чтения
  - `order-service` — транзакции и критичная логика
  - `notification-service` — асинхронная реакция на события
- Изоляция сбоев:
  - падение `notification-service` не блокирует создание заказа
- Централизованная инфраструктура:
  - Discovery + Config упрощают управление средой и адресацией сервисов

---

## 4) Коммуникации и интеграция

### Синхронные вызовы (OpenFeign)
Используется Spring Cloud OpenFeign для сервис-сервис HTTP вызовов по имени сервиса (через Eureka).

- `order-service` → `catalog-service` (Feign)
  - получение актуальных данных по позициям/ценам при создании заказа
- `payment-service` / `delivery-service` / `notification-service` → `order-service` (Feign)
  - обновление статуса заказа по результатам обработки события

### Асинхронные события (RabbitMQ)
Используется единый exchange доменных событий:

- Exchange: `food.events`
- Routing keys:
  - `order.created`
  - `payment.paid`
  - `delivery.taken`
  - `delivery.delivered`

Константы в проекте:
```java
public final class AppConstants {
    private AppConstants() {}

    public static final String EXCHANGE_EVENTS = "food.events";
    public static final String RK_ORDER_CREATED = "order.created";
    public static final String RK_PAYMENT_PAID = "payment.paid";
    public static final String RK_DELIVERY_TAKEN = "delivery.taken";
    public static final String RK_DELIVERY_DELIVERED = "delivery.delivered";
}
```
Примечания:
- `payment-service`, `delivery-service`, `notification-service` — stateless (в БД ничего не сохраняют)
- DLQ в текущей реализации отсутствует

---

## 5) Реализованные сценарии

### Сценарий A — Просмотр каталога
- Клиент → API Gateway → `catalog-service`
- Пользователь получает список ресторанов и меню (категории/позиции)

### Сценарий B — Создание заказа и обработка (MVP)
1) Клиент → API Gateway → `order-service` (создание заказа)
2) `order-service` синхронно обращается к `catalog-service` через OpenFeign
   - получает данные по позициям/ценам
   - формирует “снимок” заказа (denormalization на момент покупки)
3) `order-service` сохраняет заказ и публикует событие `order.created` в RabbitMQ (`food.events`)
4) `payment-service` потребляет `order.created`
   - эмулирует оплату
   - обновляет статус заказа в `order-service` через Feign
   - публикует `payment.paid`
5) `delivery-service` потребляет `payment.paid`
   - эмулирует этапы доставки
   - обновляет статусы заказа в `order-service` через Feign
   - публикует `delivery.taken`, затем `delivery.delivered`
6) `notification-service` потребляет доменные события
   - эмулирует уведомления (например, логирование)
   - при необходимости обновляет статус заказа в `order-service` через Feign (если в контракте предусмотрено)

### Сценарий C — Наблюдаемость
- Запросы через Gateway трассируются
- В Zipkin можно увидеть цепочки вызовов (например: `gateway` → `order-service` → `catalog-service`) и дальнейшую обработку

---

## 6) Нефункциональные требования (реализовано)

### Наблюдаемость
- Spring Boot Actuator:
  - `/actuator/health/liveness`
  - `/actuator/health/readiness`
- Distributed tracing:
  - Micrometer Tracing
  - экспорт трасс в Zipkin

### Отказоустойчивость на уровне запуска окружения
- Docker Compose поднимает инфраструктуру и сервисы как единый стенд
- Используются `healthcheck` и `depends_on` для корректного порядка старта:
  - бизнес-сервисы запускаются после готовности инфраструктурных зависимостей (Eureka/Config/Postgres/RabbitMQ/Zipkin)

---

## 7) Технологический стек

### Backend
- Java 21
- Spring Boot 3.5.x
- Spring Web (REST)
- Spring Validation
- Spring Data JPA + Hibernate
- Flyway
- Spring AMQP (RabbitMQ)
- Spring Boot Actuator
- Micrometer Tracing + Zipkin
- springdoc-openapi (Swagger/OpenAPI)

### Spring Cloud
- Eureka Discovery Server
- Spring Cloud Config Server (native)
- Spring Cloud Gateway
- OpenFeign

### Infrastructure
- Docker / Docker Compose
- PostgreSQL
- RabbitMQ (+ Management UI)
- Zipkin

---

## 8) Запуск проекта

```bash
docker compose up -d --build