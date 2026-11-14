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
COPY ms-clientes/src ./ms-clientes/src

RUN mvn clean package -pl ms-clientes -am -DskipTests

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS final

WORKDIR /app

# Creamos el grupo y usuario
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# ==========================================
# CAMBIO CLAVE: Creamos la carpeta data y le damos permisos al usuario
# ==========================================
RUN mkdir -p /app/data && chown -R appuser:appgroup /app/data

COPY --from=build --chown=appuser:appgroup /app/ms-clientes/target/ms-clientes-1.0.0-SNAPSHOT.jar /app/app.jar

USER appuser

EXPOSE 41074

ENTRYPOINT [ "java", "-jar", "app.jar" ]