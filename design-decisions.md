# Design Decisions — Сервис доставки еды

## Почему микросервисы
- Независимое развитие и масштабирование: Catalog/Menu (много чтения), Order (транзакции), Notification (асинхронность).
- Изоляция сбоев: падение Notification не блокирует создание заказа.

## Ключевые решения
- **Eureka Discovery Server:** сервисы находят друг друга по имени, без хардкода адресов.
- **Spring Cloud Gateway:** единая точка входа, маршрутизация.
- **Spring Cloud Config Server (native):** централизованные конфиги из файлов, единые параметры (например, JWT keys, RabbitMQ, Zipkin endpoint).
- **JWT Security:** централизованная выдача JWT (Auth Service) + локальная валидация JWT во всех сервисах (без сетевых вызовов на каждый запрос).
- **RabbitMQ:** асинхронные доменные события (например, order.created) и доставка уведомлений.
- **Order denormalization:** name_at_moment, price_at_moment хранятся в Order Service, чтобы история заказа не зависела от изменений меню.
- **Resilience4j (Circuit Breaker/Retry/TimeLimiter):** защита синхронных вызовов Order -> Catalog/Menu от каскадных сбоев.
- **Хранилища:** в dev-окружении используется один Postgres контейнер; данные сервисов разделены схемами/миграциями (Flyway). В production-дизайне допускается выделение отдельных БД.

## Наблюдаемость
- **Actuator:** health/readiness/liveness, метрики.
- **Distributed tracing:** Micrometer Tracing с экспортом в Zipkin.

Для обеспечения безопасности в микросервисной архитектуре выбран подход с централизованной выдачей JWT-токенов (Auth Service) 
и локальной валидацией токенов во всех сервисах. 
Это позволяет избежать сетевых вызовов к Auth Service при каждом запросе, снижает задержку и повышает отказоустойчивость. 
Ролевая модель реализована через стандартные механизмы Spring Security (hasAuthority), а управление пользователями вынесено в отдельный сервис.

Для централизованного управления конфигурациями используется Spring Cloud Config Server в режиме native (чтение из файловой системы). 
Это позволяет:
- Хранить все настройки в едином репозитории.
- Обеспечить согласованность значений (например, JWT_SECRET) между всеми микросервисами.
- Обновлять конфигурации без перезапуска сервисов (через Actuator + @RefreshScope).
- Реализовать fail-fast поведение: сервис не стартует, если не может получить конфигурацию, что повышает надёжность системы.

## Поток создания заказа
1. Клиент -> API Gateway -> Order Service (создание заказа).
2. Order Service синхронно запрашивает позиции/цены из Catalog/Menu Service (Feign).
3. Order Service сохраняет заказ со “снимком” позиций.
4. Order Service публикует событие order.created в RabbitMQ.
5. Notification Service потребляет событие и создаёт уведомление.

## Что покрыто стеком Spring
- Spring Boot, Spring Data JPA, Spring Web, Spring Cloud Gateway, Eureka, Resilience4j, Spring AMQP, Actuator, Micrometer Tracing + Zipkin exporter.
