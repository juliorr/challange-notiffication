# syntax=docker/dockerfile:1.6

# ---------- dev stage: Maven + JDK, source mounted as a volume in docker-compose ----------
FROM maven:3.9-eclipse-temurin-21 AS dev
WORKDIR /app
# Pre-resolve dependencies to speed up first boot (pom is copied, sources come from the volume).
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline || true
EXPOSE 8080
CMD ["mvn", "spring-boot:run"]

# ---------- build stage: produces the fat jar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- runtime stage: slim JRE, copy the jar ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
