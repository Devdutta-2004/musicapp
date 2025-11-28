# ---- BUILD STAGE ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven wrapper and config
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (faster rebuild)
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline -B

# Copy source code and build
COPY . .
RUN ./mvnw -q -DskipTests package

# ---- RUN STAGE ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
