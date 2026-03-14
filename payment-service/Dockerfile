# ===== STAGE 1: BUILD =====
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

# ===== STAGE 2: RUNTIME =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]