FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY common-events/build.gradle ./common-events/build.gradle
COPY common-events/src ./common-events/src
COPY storage-service/build.gradle ./storage-service/build.gradle
COPY storage-service/src ./storage-service/src
RUN ./gradlew :storage-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /workspace/storage-service/build/libs/storage-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
