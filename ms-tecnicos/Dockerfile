FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ms-tecnicos-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 41073
ENTRYPOINT ["java", "-jar", "app.jar"]