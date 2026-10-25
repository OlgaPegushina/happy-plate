# HappyPlate — микросервисная платформа доставки еды
(Java 21 и Spring Boot 3.5.x.)

Фокус проекта:
- инфраструктурные компоненты (Gateway, Discovery, Config)
- синхронные вызовы между сервисами через OpenFeign
- асинхронное взаимодействие через RabbitMQ (доменные события)
- безопасность (Auth Service + JWT)
- наблюдаемость (Actuator + Zipkin tracing)
- запуск целиком через Docker Compose

---

## 1) Состав системы

### Карта сервисов (порты)

| Компонент                   |  Порт | Назначение                                                    |
|-----------------------------|------:|---------------------------------------------------------------|
| `gateway-server`            |  8080 | Единая точка входа, маршрутизация запросов                    |
| `auth-server`               |  9000 | Аутентификация/авторизация, выдача JWT, публикация JWKS       |
| `catalog-service`           |  8081 | Каталог ресторанов, категорий и меню                          |
| `order-service`             |  8082 | Заказы: создание, статусы, “снимок” заказа                    |
| `payment-service`           |  8084 | Заглушка оплаты (stateless), реакция на события               |
| `delivery-service`          |  8083 | Заглушка доставки (stateless), реакция на события             |
| `notification-service`      |  8085 | Заглушка уведомлений (stateless), реакция на события          |
| `discovery-server` (Eureka) |  8761 | Реестр сервисов                                               |
| `config-server`             |  8888 | Централизованные конфиги (native)                             |
| `zipkin`                    |  9411 | Distributed tracing                                           |
| `rabbitmq` (AMQP)           |  5672 | Брокер сообщений                                              |
| `rabbitmq` (UI)             | 15672 | RabbitMQ Management                                           |
| `postgres`                  |  5432 | База данных dev-окружения                                     |
| `interaction-api` | - | Библиотека общих данных сервисов: ДТО, enum, event, константы |

---

## 2) Ответственность сервисов

### Инфраструктура
- `discovery-server` (Eureka)
  - сервисы регистрируются и находят друг друга по имени (`http://<service-name>`)
- `config-server` (Spring Cloud Config, native)
  - централизованная раздача конфигурации (общий + сервисные `application.yml`)
- `gateway-server` (Spring Cloud Gateway)
  - единая точка входа, маршрутизация, проксирование Swagger-спецификаций
  - работает как OAuth2 Resource Server (валидация JWT по JWKS)
- `zipkin`
  - сбор распределённых трасс
- `rabbitmq`
  - транспорт доменных событий
- `postgres`
  - единый контейнер PostgreSQL для dev
  - миграции через Flyway

### Бизнес-сервисы
- `auth-service`
    - регистрация пользователя (MVP)
    - логин (получение access_token в формате JWT)
    - публикация публичного ключа подписи через JWKS endpoint
    - bootstrap администратора через env (`APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD`)
    - хранение пользователей/ролей в Postgres (схема `identity`)
- `catalog-service`
  - хранит и отдаёт каталог (рестораны, категории, позиции меню)
  - CRUD/API для управления каталогом (схема `catalog`)
- `order-service`
  - создаёт заказ и управляет статусом заказа (схема `orders`)
  - хранит “снимок” заказа на момент покупки (денормализация: название/цена на момент заказа)
  - проверяет блюдо заказа в `catalog-service` через Feign
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
  - эмулирует уведомления (запись в лог)

---

## 3) Почему микросервисы (Design Decisions)

- Независимое развитие и масштабирование:
  - `catalog-service` — много чтения
  - `order-service` — транзакции и критичная логика
  - `notification-service` — асинхронная реакция на события
  - `auth-service` — отдельный security-контур (ключи, пользователи, токены)
- Изоляция сбоев:
  - падение `notification-service` не блокирует создание заказа
- Централизованная инфраструктура:
  - Discovery + Config упрощают управление средой и адресацией сервисов
- Разделение данных по схемам (schema-per-service в одной БД):
  - `identity` (auth), `catalog`, `orders` — логическая изоляция данных сервисов
  
---

## 4) Коммуникации и интеграция

### Синхронные вызовы (OpenFeign)
Используется Spring Cloud OpenFeign для сервис-сервис HTTP вызовов по имени сервиса (через Eureka).

- `order-service` -> `catalog-service` (Feign)
  - получение актуальных данных по позициям/ценам при создании заказа
- `payment-service` / `delivery-service` -> `order-service` (Feign)
  - обновление статуса заказа по результатам обработки события

Внутренние вызовы защищены заголовком `X-Internal-Token` (см. `AppConstants.HEADER`) —
чтобы сервис-сервис запросы (например, обновление статуса заказа) не требовали пользовательского JWT,
но при этом не были открыты наружу.

### Аутентификация/авторизация (JWT + JWKS)

- `auth-service` выдаёт `access_token` (JWT), подписанный приватным RSA-ключом
- `auth-service` публикует публичный ключ через JWKS:
    - GET http://auth-service:9000/.well-known/jwks.json
- `gateway-server` и backend-сервисы работают как OAuth2 Resource Server:
    - валидируют JWT по JWKS (без шаринга секретов между сервисами)
- Кастомные claim-ы в токене:
  - `user_id` (`AppConstants.CLAIM_USER_ID`)
  - `roles` (`AppConstants.CLAIM_ROLES`)
  
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
  // RabbitMQ exchange для доменных событий системы (event-driven интеграция).
  // Сюда публикуются события из order/payment/delivery, и отсюда их потребляют слушатели.
  public static final String EXCHANGE_EVENTS = "food.events";
   // Routing key: заказ создан (order-service публикует после сохранения заказа).
   // Потребитель: payment-service (стартует обработку оплаты).
  public static final String RK_ORDER_CREATED = "order.created";
   // Routing key: заказ оплачен (payment-service публикует после успешной "оплаты").
   // Потребитель: delivery-service (стартует доставку).
  public static final String RK_PAYMENT_PAID = "payment.paid";
   // Routing key: доставка взята в работу (delivery-service публикует на промежуточном этапе).
   // Потребитель: notification-service (и любые другие реактивные обработчики в будущем).
  public static final String RK_DELIVERY_TAKEN = "delivery.taken";
   // Routing key: доставка завершена (delivery-service публикует финальное событие).
   // Потребитель: notification-service (и любые другие реактивные обработчики в будущем).
  public static final String RK_DELIVERY_DELIVERED = "delivery.delivered";
   // Очередь для notification-service: получает все доменные события для "уведомлений" (логирование).
  public static final String Q_NOTIFICATIONS = "notification.events.q";
   // Очередь для delivery-service: получает событие "оплачено" (payment.paid),
   // после чего delivery-service эмулирует этапы доставки.
  public static final String Q_DELIVERY_PAYMENT_PAID = "delivery.payment-paid.q";
   // Очередь для payment-service: получает событие "заказ создан" (order.created),
   // после чего payment-service эмулирует оплату.
  public static final String Q_PAYMENT_ORDER_CREATED = "payment.order-created.q";
   // JWT claim: идентификатор пользователя (кладётся auth-service в access_token).
   // Используется сервисами для авторизации/аудита.
  public static final String CLAIM_USER_ID = "user_id";
   // JWT claim: роли пользователя (например, USER/ADMIN).
   // Используется при проверке доступа к защищённым endpoint-ам.
  public static final String CLAIM_ROLES = "roles";
   // Заголовок для внутренних запросов между сервисами (service-to-service).
   // Например: payment-service/delivery-service обновляют статус заказа в order-service.
  public static final String HEADER = "X-Internal-Token";
   // Разрешённые переходы статусов заказа.
   // Используется order-service для валидации смены статуса.
  public static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
          DRAFT, Set.of(CREATED),
          CREATED, Set.of(PAID),
          PAID, Set.of(DELIVERY_TAKEN),
          DELIVERY_TAKEN, Set.of(DELIVERED),
          DELIVERED, Set.of()
  );
}
```
Жизненный цикл заказа (state machine):
`DRAFT -> CREATED -> PAID -> DELIVERY_TAKEN -> DELIVERED`
Переходы контролируются картой `ALLOWED` — недопустимый переход статуса отклоняется.

Настройки RabbitMQ-листенеров (из общего `application.yml`):
- `default-requeue-rejected: false` — отклонённые сообщения не возвращаются в очередь бесконечно
- `missing-queues-fatal: false` — сервис не падает, если очередь ещё не объявлена
- `declaration-retries: 60`, `failed-declaration-retry-interval: 5000` — переживаем «медленный» старт RabbitMQ

Примечания:
- `payment-service`, `delivery-service`, `notification-service` — stateless (в БД ничего не сохраняют)
- DLQ в текущей реализации отсутствует

---

## 5) Реализованные сценарии

### Сценарий A — Просмотр каталога
- Клиент -> API Gateway -> `catalog-service`
- Пользователь получает список ресторанов и меню (категории/позиции)

### Сценарий В — Регистрация/логин и доступ к защищённым ресурсам
- Клиент -> API Gateway -> `auth-service`
- Регистрация (MVP):
    - создаётся пользователь (логин/пароль)
    - пароль хранится в виде хеша (BCrypt)
- Логин:
    - `auth-service` возвращает `access_token` (JWT)
- Клиент вызывает защищённые эндпоинты через Gateway с заголовком:
    - `Authorization: Bearer <token>`
- Gateway и/или сервисы валидируют токен по JWKS auth-service

### Сценарий C — Создание заказа и обработка (MVP)
1) Клиент -> API Gateway -> `order-service` (создание заказа)
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
   - эмулирует уведомления (логирование)

### Сценарий D — Наблюдаемость
- Запросы через Gateway трассируются
- В Zipkin можно увидеть цепочки вызовов (например: `gateway` -> `order-service` -> `catalog-service`) и дальнейшую обработку

---

## 6) Нефункциональные требования (реализовано)

### Наблюдаемость
- Spring Boot Actuator:
  - `/actuator/health`
  - `/actuator/health/liveness`
  - `/actuator/health/readiness`
  - `/actuator/info`
- Distributed tracing:
  - Micrometer Tracing
  - экспорт трасс в Zipkin

### Отказоустойчивость на уровне запуска окружения
- Docker Compose поднимает инфраструктуру и сервисы как единый стенд
- Используются `healthcheck` и `depends_on` для корректного порядка старта:
  - бизнес-сервисы запускаются после готовности инфраструктурных зависимостей (Eureka/Config/Postgres/RabbitMQ/Zipkin) и сервиса авторизации (Auth)
- `start_period` в healthcheck учитывает «тяжёлый» старт сервисов (JVM + Flyway + подключение к Config/Eureka)
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
- Lombok

### Security
- OAuth2 Resource Server (валидация JWT по JWKS)
- Выдача JWT в `auth-service` (RSA private/public keys)
- Хеширование паролей: BCrypt
- Внутренний трафик сервис-сервис: заголовок `X-Internal-Token`

### Spring Cloud
- Eureka Discovery Server
- Spring Cloud Config Server (native)
- Spring Cloud Gateway
- OpenFeign

### Infrastructure
- Docker / Docker Compose
- PostgreSQL (схемы: `identity`, `catalog`, `orders`)
- RabbitMQ (+ Management UI)
- Zipkin

---

## 8) Запуск проекта

### Предварительные требования
- Установлен Docker Desktop (с Docker Compose v2)
- Свободны порты: `8080`, `8761`, `8888`, `9000`, `9411`, `5672`, `15672`, и порт Postgres
- (для варианта A генерации ключей) установлен JDK 21 / IntelliJ IDEA

## - Генерация JWT ключей (auth-service)

JWT-ключи используются **только auth-service**:
- `jwt-private.pem` — подпись токенов
- `jwt-public.pem` — публикация публичного ключа (JWKS) для проверки токенов другими сервисами

Ключи должны лежать в папке:

```text
./secrets/jwt-private.pem
./secrets/jwt-public.pem

```

### Вариант A (рекомендуется для Windows/IDEA): генерация через Java

1. Убедитесь, что в проекте есть утилита генерации ключей (например `tools/KeyGen.java`), которая создаёт файлы в `./secrets`.
2. Запустите `KeyGen.main()` из IntelliJ IDEA:
    - Откройте `KeyGen.java`
    - Нажмите Run на `public static void main(...)`
3. Проверьте, что файлы появились и не пустые:
    - `secrets/jwt-private.pem` начинается с `-----BEGIN PRIVATE KEY-----`
    - `secrets/jwt-public.pem` начинается с `-----BEGIN PUBLIC KEY-----`

### Вариант B: генерация через Docker (без установки OpenSSL)

Запускать команды из **корня репозитория**.

> Ниже примеры для PowerShell (Windows). В bash/macOS/Linux замените `${PWD}\secrets` на `${PWD}/secrets`.

```powershell
mkdir secrets
```

Сгенерировать private key:
```powershell
docker run --rm -v ${PWD}\secrets:/secrets alpine:3.20 sh -lc "apk add --no-cache openssl >/dev/null && openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out /secrets/jwt-private.pem"
```

Сгенерировать public key:
```powershell
docker run --rm -v ${PWD}\secrets:/secrets alpine:3.20 sh -lc "apk add --no-cache openssl >/dev/null && openssl rsa -pubout -in /secrets/jwt-private.pem -out /secrets/jwt-public.pem"
```
### Запуск через Docker Compose
В корне репозитория:
```bash
docker compose up -d --build
```

## - Остановить и удалить контейнеры:
```bash 
docker compose down
```
## - Остановить и удалить контейнеры + volumes (сотрёт данные Postgres/RabbitMQ):
```bash 
docker compose down -v
```

## Docker build (единый Dockerfile для всех сервисов)

В проекте используется **один общий `Dockerfile` в корне репозитория**.  
Какой именно сервис (модуль Maven) собирать — задаётся аргументом сборки `MODULE_PATH`.

### Результат
- Единый стандарт сборки для всех сервисов (одинаковый JDK/JRE, параметры JVM, TZ).
- Уменьшение дублирования: не нужно поддерживать `Dockerfile` в каждом модуле.
- Быстрее пересборки за счёт кеширования слоёв:
  - сначала копируются только `pom.xml`,
  - выполняется прогрев зависимостей `dependency:go-offline`,
  - исходники копируются только после этого.
- Каждый сервис в docker-compose.yml использует один и тот же контекст и Dockerfile, 
но передаёт свой MODULE_PATH

---
## 9) Полезные ссылки (локально)

| Компонент | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Auth Service | http://localhost:9000 |
| Auth JWKS | http://localhost:9000/.well-known/jwks.json |
| Eureka Dashboard | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| Zipkin | http://localhost:9411 |
| RabbitMQ Management | http://localhost:15672 |
| Swagger (через Gateway) | http://localhost:8080/swagger-ui.html |

> RabbitMQ Management по умолчанию: логин/пароль из env (`RABBITMQ_USER` / `RABBITMQ_PASSWORD`, по умолчанию `app` / `app_pass`).

---

### Полезные диагностические команды

```powershell
# Статус сервисов (healthy/unhealthy)
docker compose ps

# Логи конкретного сервиса
docker compose logs -f --tail 200 auth-service

# Причина падения healthcheck
$cid = docker compose ps -q auth-service
docker inspect $cid --format '{{json .State.Health}}'

# Проверка health/readiness изнутри контейнера
docker compose exec auth-service curl -i http://localhost:9000/actuator/health
docker compose exec auth-service curl -i http://localhost:9000/actuator/health/readiness
```