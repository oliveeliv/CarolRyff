# 1. Usamos la imagen base de Maven con Temurin
FROM maven:3.8.5-eclipse-temurin-17 AS build

# 2. Definimos la carpeta raíz del contenedor de Docker
WORKDIR /app

# 3. Copiamos los archivos de tu proyecto al contenedor
COPY . .

# 4. Compilamos el proyecto (¡aquí ya va a encontrar el pom.xml directo!)
RUN mvn clean package -DskipTests

# 5. Segunda etapa para correr la aplicación con un JDK ligero
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# 6. Exponemos el puerto de Render y ejecutamos el .jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]