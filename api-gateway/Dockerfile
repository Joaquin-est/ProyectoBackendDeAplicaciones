# ==========================================
# Stage 1: Build
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiamos todos los pom.xml (multi-módulo)
COPY pom.xml ./
COPY commons-geo/pom.xml ./commons-geo/
COPY ms-clientes/pom.xml ./ms-clientes/
COPY ms-tecnicos/pom.xml ./ms-tecnicos/
COPY ms-solicitudes/pom.xml ./ms-solicitudes/
COPY api-gateway/pom.xml ./api-gateway/

# Copiamos el código fuente necesario
COPY commons-geo/src ./commons-geo/src
COPY api-gateway/src ./api-gateway/src

# Compilamos solo el api-gateway y sus dependencias
RUN mvn clean package -pl api-gateway -am -DskipTests

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS final

WORKDIR /app

# Creamos usuario no root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiamos el JAR generado
COPY --from=build --chown=appuser:appgroup /app/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar /app/app.jar

USER appuser

EXPOSE 41071

ENTRYPOINT [ "java", "-jar", "app.jar" ]