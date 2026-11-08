FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ms-clientes-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 41074
ENTRYPOINT ["java", "-jar", "app.jar"]