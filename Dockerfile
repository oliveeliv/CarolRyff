
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
WORKDIR /carolRyff
RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre-jammy
COPY --from=build /carolRyff/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]