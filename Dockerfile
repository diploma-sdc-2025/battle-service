
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update && \
    apt-get install -y stockfish && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN useradd -m appuser
USER appuser

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","app.jar"]