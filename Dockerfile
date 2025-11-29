FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Копируем только необходимые файлы для сборки
COPY build.gradle .
COPY settings.gradle .
COPY gradle gradle
COPY gradlew .

# Делаем gradlew исполняемым
RUN chmod +x gradlew

# Копируем исходники
COPY src src

# Собираем проект
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
