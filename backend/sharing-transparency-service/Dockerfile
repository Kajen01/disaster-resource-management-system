FROM eclipse-temurin:21-jre
WORKDIR /app
COPY backend/sharing-transparency-service/target/sharing-transparency-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
