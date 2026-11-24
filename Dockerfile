FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /app

COPY pom.xml .
COPY settings.xml .
COPY src src

RUN mvn -s settings.xml clean package -DskipTests

FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]