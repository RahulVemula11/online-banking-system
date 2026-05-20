# ---- Stage 1: build the jar ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Download dependencies first (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: run ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render/Railway provide DATABASE_URL as postgresql://user:pass@host:port/db
# Spring needs jdbc:postgresql://host:port/db plus separate user/pass.
# This entrypoint converts it if JDBC_DATABASE_URL isn't already set.
COPY docker-entrypoint.sh ./
RUN chmod +x docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["./docker-entrypoint.sh"]
