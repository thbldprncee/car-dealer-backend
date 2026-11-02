# syntax=docker/dockerfile:1
# Builds the monolith from commit 5baba7b (the last commit before the split).
# run.sh exports that commit with `git archive` and uses it as the build context,
# so the image is packaged exactly like order-service and storage-service.

FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew bootJar --no-daemon -q -x test \
    && java -Djarmode=tools -jar "$(ls build/libs/*.jar | grep -v plain)" extract --layers --launcher --destination /layers

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /layers/dependencies/ ./
COPY --from=build /layers/spring-boot-loader/ ./
COPY --from=build /layers/snapshot-dependencies/ ./
COPY --from=build /layers/application/ ./
USER app
EXPOSE 8080
# The monolith has no actuator; the OpenAPI document is public and needs the database.
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=10 \
    CMD wget -qO- http://localhost:8080/v3/api-docs > /dev/null || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
