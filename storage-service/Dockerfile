# syntax=docker/dockerfile:1
# Build context is the repository root: the service depends on common-events and common-grpc.

FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY common-events common-events
COPY common-grpc common-grpc
COPY storage-service storage-service
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew :storage-service:bootJar --no-daemon -q -x test \
    && java -Djarmode=tools -jar storage-service/build/libs/storage-service-*.jar extract --layers --launcher --destination /layers

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /layers/dependencies/ ./
COPY --from=build /layers/spring-boot-loader/ ./
COPY --from=build /layers/snapshot-dependencies/ ./
COPY --from=build /layers/application/ ./
USER app
EXPOSE 8081 9090
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=10 \
    CMD wget -qO- http://localhost:8081/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
