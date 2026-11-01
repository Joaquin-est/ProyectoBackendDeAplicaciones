FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -pl services/ms-clientes -am package -DskipTests
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/services/ms-clientes/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
