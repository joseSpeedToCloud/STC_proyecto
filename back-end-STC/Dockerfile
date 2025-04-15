# Etapa 1: Construcción de la aplicación usando Java 17
FROM openjdk:17-jdk-slim as build

WORKDIR /app

# Copiar archivos necesarios para Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargar dependencias para uso offline
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src src

# Compilar la aplicación sin ejecutar pruebas
RUN ./mvnw package -DskipTests

# Extraer el contenido del JAR al directorio target/dependency
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Etapa 2: Configuración del entorno para el backend y base de datos
FROM openjdk:17-jdk-slim

# Instalar dependencias necesarias, incluyendo PostgreSQL y AWS CLI
RUN apt-get update && apt-get install -y curl python3 postgresql postgresql-contrib unzip

# Instalar AWS CLI
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf aws awscliv2.zip

# Verificar la instalación de AWS CLI
RUN aws --version

# Instalar el SDK de Google Cloud
RUN curl -sSL https://sdk.cloud.google.com | bash

# Agregar gcloud al PATH
ENV PATH=$PATH:/root/google-cloud-sdk/bin

# Verificar la instalación de gcloud
RUN gcloud --version

# Configurar PostgreSQL
USER postgres
RUN service postgresql start && \
    psql -tc "SELECT 1 FROM pg_roles WHERE rolname = 'postgres';" | grep -q 1 || \
    psql -c "CREATE USER postgres WITH ENCRYPTED PASSWORD 'postgres';" && \
    psql -tc "SELECT 1 FROM pg_database WHERE datname = 'cloud_resources';" | grep -q 1 || \
    psql -c "CREATE DATABASE cloud_resources;" && \
    psql -c "GRANT ALL PRIVILEGES ON DATABASE cloud_resources TO postgres;"

USER root

# Variables de entorno para la conexión a PostgreSQL
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cloud_resources \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=postgres

ARG DEPENDENCY=/app/target/dependency

# Copiar las dependencias y archivos de la aplicación desde la etapa de construcción
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Exponer el puerto 8080 para la aplicación
EXPOSE 8080

# Configurar el comando de entrada para ejecutar la aplicación
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.gestion_backend.GestionBackendBackendApplication"]

