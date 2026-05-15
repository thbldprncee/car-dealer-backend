FROM eclipse-temurin:21-jre

WORKDIR /app
COPY storage-service/build/libs/storage-service-*.jar app.jar
EXPOSE 8081 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
