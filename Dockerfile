
FROM maven:3.8.5-openjdk-17 AS build
COPY . .

WORKDIR /carolRyff
RUN mvn clean package -DskipTests


FROM openjdk:17-jdk-slim
COPY --from=build /carolRyff/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]