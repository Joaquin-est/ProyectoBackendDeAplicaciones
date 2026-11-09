# ==========================================
# Stage 1: Build
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml ./
COPY commons-geo/pom.xml ./commons-geo/
COPY ms-clientes/pom.xml ./ms-clientes/
COPY ms-tecnicos/pom.xml ./ms-tecnicos/
COPY ms-solicitudes/pom.xml ./ms-solicitudes/
COPY api-gateway/pom.xml ./api-gateway/

COPY commons-geo/src ./commons-geo/src
COPY ms-solicitudes/src ./ms-solicitudes/src

RUN mvn clean package -pl ms-solicitudes -am -DskipTests

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS final

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build --chown=appuser:appgroup /app/ms-solicitudes/target/ms-solicitudes-1.0.0-SNAPSHOT.jar /app/app.jar

USER appuser

EXPOSE 41072

ENTRYPOINT [ "java", "-jar", "app.jar" ]