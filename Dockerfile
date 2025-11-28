# ---- BUILD STAGE ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the files required to download dependencies first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the mvnw executable
RUN chmod +x mvnw

# Download dependencies and build the application (no go-offline)
COPY . .
RUN ./mvnw -B -DskipTests package

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
