FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY common-events/build.gradle ./common-events/build.gradle
COPY common-events/src ./common-events/src
COPY order-service/build.gradle ./order-service/build.gradle
COPY order-service/src ./order-service/src
RUN ./gradlew :order-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /workspace/order-service/build/libs/order-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
