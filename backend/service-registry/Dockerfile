FROM eclipse-temurin:21-jre
WORKDIR /app
COPY backend/service-registry/target/service-registry-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
