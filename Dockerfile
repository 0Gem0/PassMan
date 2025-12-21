FROM ubuntu:latest
LABEL authors="gmull"

# ===== STAGE 1: build =====
FROM openjdk:21-jdk-slim AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests
# ===== STAGE 1: build =====
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем только pom.xml для кеширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходники
COPY src ./src

# Собираем jar
RUN mvn clean package -DskipTests


# ===== STAGE 2: run =====
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Копируем jar из build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]