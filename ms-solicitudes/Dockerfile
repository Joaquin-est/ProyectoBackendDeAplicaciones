FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ms-solicitudes-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 41072
ENTRYPOINT ["java", "-jar", "app.jar"]