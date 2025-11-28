# ---- BUILD STAGE ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the files required to download dependencies first (speeds caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the mvnw executable (initial; will set again after copying full repo)
RUN chmod +x mvnw

# Copy the rest of the source (this may overwrite mvnw's mode on some filesystems)
COPY . .

# Ensure mvnw is executable after full copy (fixes permission lost by COPY)
RUN chmod +x mvnw

# Build the application (skip tests to speed up)
RUN ./mvnw -B -DskipTests package

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
