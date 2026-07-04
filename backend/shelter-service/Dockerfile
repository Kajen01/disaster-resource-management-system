FROM eclipse-temurin:21-jre
WORKDIR /app
COPY backend/shelter-service/target/shelter-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
