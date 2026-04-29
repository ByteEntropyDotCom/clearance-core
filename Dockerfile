# Stage 1: Build using Maven and JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Package the application (skipping tests for faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image using JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/clearance-core-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
