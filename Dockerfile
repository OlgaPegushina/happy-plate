# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder

ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /workspace

ARG MODULE_PATH

# 1) Родительские POM'ы
COPY pom.xml ./pom.xml
COPY core/pom.xml ./core/pom.xml
COPY infra/pom.xml ./infra/pom.xml

# 2) POM'ы ВСЕХ модулей
#    Сохраняем пути core/<module>/pom.xml и infra/<module>/pom.xml
COPY --parents core/*/pom.xml ./
COPY --parents infra/*/pom.xml ./

# 3) Кэш зависимостей
RUN mvn -B -pl ${MODULE_PATH} -am dependency:go-offline

# 4) Исходники
COPY core ./core
COPY infra ./infra

# 5) Сборка
RUN mvn -B -pl ${MODULE_PATH} -am clean package -DskipTests

# 6) Исполняемый jar
RUN set -eux; \
    JAR="$(ls -1 ${MODULE_PATH}/target/*.jar | grep -v '\.original$' | head -n 1)"; \
    cp "$JAR" /workspace/app.jar


FROM eclipse-temurin:21-jre-jammy

ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt-get update && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Europe/Moscow"

COPY --from=builder /workspace/app.jar /app.jar
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app.jar"]