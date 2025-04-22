# Koristi JDK 17 kao osnovnu sliku
FROM openjdk:17-jdk-slim

# # Postavi radni direktorijum u kontejneru
WORKDIR /app

# # Kopiraj JAR fajl (projekat treba da bude već izgrađen sa `mvn package`)
COPY target/*.jar app.jar

# # Eksponuj port 8080 (isti kao u Spring Boot aplikaciji)
EXPOSE 8080

# # Pokreni aplikaciju
ENTRYPOINT ["java", "-jar", "app.jar"]


# Stage 1: Build aplikacije
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Pokretanje aplikacije
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/techshop-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]