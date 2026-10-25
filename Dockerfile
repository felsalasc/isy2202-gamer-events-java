FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copiar Maven Wrapper y configuración
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Descargar dependencias en caché
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Crear directorio para la base de datos persistente H2
RUN mkdir -p /app/data

COPY --from=build /app/target/gamer-events-0.0.1-SNAPSHOT.jar app.jar

# Configuración por defecto para entorno Docker
ENV SERVER_ADDRESS=0.0.0.0 \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=demo

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
