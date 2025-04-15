package com.gestion_backend.controller_bd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;

@Component
public class IniciarBD {
    private static final Logger logger = LoggerFactory.getLogger(IniciarBD.class);

    @Value("${spring.datasource.url.sinbd}")
    private String BD_URL_SIN_BD;

    @Value("${spring.datasource.url}")
    private String BD_URL;

    @Value("${spring.datasource.nombre}")
    private String BD_NAME;

    @Value("${spring.datasource.username}")
    private String BD_USER;

    @Value("${spring.datasource.password}")
    private String BD_PASSWORD;

    @PostConstruct
    public void init() {
        logger.info("Iniciando verificación de base de datos al arrancar la aplicación (PostConstruct)");
        try {
            creacionBD();
        } catch (SQLException e) {
            logger.error("Error crítico al inicializar la base de datos en PostConstruct: " + e.getMessage(), e);
            // No relanzamos la excepción para permitir que la aplicación continúe
        }
    }

    @Bean
    public CommandLineRunner iniciarBaseDeDatos() {
        return args -> {
            logger.info("Verificando base de datos mediante CommandLineRunner");
            try {
                creacionBD();
                logger.info("Base de datos y tablas verificadas exitosamente mediante CommandLineRunner");
            } catch (SQLException e) {
                logger.error("Error fatal en CommandLineRunner al inicializar la base de datos: " + e.getMessage(), e);
                // Forzamos la salida de la aplicación si falla aquí
                System.exit(1);
            }
        };
    }

    public void creacionBD() throws SQLException {
        logger.info("Iniciando creación/verificación de la base de datos: {}", BD_NAME);

        esperarPostgreSQLListo();
        crearBaseDeDatosSiNoExiste();
        verificarYOtorgarPermisos();
        crearTablasConReintentos();
    }

    private void esperarPostgreSQLListo() throws SQLException {
        logger.info("Esperando a que PostgreSQL esté listo...");
        final int maxIntentos = 15;
        final int tiempoEsperaMs = 2000;

        for (int intento = 1; intento <= maxIntentos; intento++) {
            try (Connection conn = DriverManager.getConnection(BD_URL_SIN_BD, BD_USER, BD_PASSWORD);
                 Statement stmt = conn.createStatement()) {

                if (stmt.execute("SELECT 1")) {
                    logger.info("PostgreSQL está listo (Intento {}/{} exitoso)", intento, maxIntentos);
                    return;
                }
            } catch (SQLException e) {
                logger.warn("Intento {}/{}: PostgreSQL no está listo aún - {}",
                        intento, maxIntentos, e.getMessage());

                if (intento == maxIntentos) {
                    throw new SQLException("PostgreSQL no está disponible después de " + maxIntentos + " intentos");
                }

                try {
                    Thread.sleep(tiempoEsperaMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupción mientras se esperaba por PostgreSQL", ie);
                }
            }
        }
    }

    private void crearBaseDeDatosSiNoExiste() throws SQLException {
        logger.info("Verificando existencia de la base de datos '{}'", BD_NAME);

        try (Connection connection = DriverManager.getConnection(BD_URL_SIN_BD, BD_USER, BD_PASSWORD)) {
            boolean dbExists = verificarExistenciaBD(connection);

            if (!dbExists) {
                crearNuevaBaseDatos(connection);
            }

            // Verificar que la nueva BD es accesible
            try (Connection testConn = DriverManager.getConnection(BD_URL, BD_USER, BD_PASSWORD)) {
                logger.info("Conexión a la base de datos '{}' verificada con éxito", BD_NAME);
            }
        }
    }

    private boolean verificarExistenciaBD(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM pg_database WHERE datname = ?")) {
            ps.setString(1, BD_NAME);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.info("Base de datos '{}' {}existe", BD_NAME, exists ? "" : "no ");
                return exists;
            }
        }
    }

    private void crearNuevaBaseDatos(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            logger.info("Creando base de datos '{}'...", BD_NAME);
            stmt.executeUpdate("CREATE DATABASE " + BD_NAME);

            // Otorgar permisos explícitos
            stmt.executeUpdate("GRANT ALL PRIVILEGES ON DATABASE " + BD_NAME + " TO " + BD_USER);
            logger.info("Base de datos '{}' creada correctamente con permisos", BD_NAME);
        }
    }

    private void verificarYOtorgarPermisos() throws SQLException {
        logger.info("Verificando y otorgando permisos en la base de datos...");

        try (Connection connection = DriverManager.getConnection(BD_URL, BD_USER, BD_PASSWORD);
             Statement stmt = connection.createStatement()) {

            // Crear esquemas si no existen
            crearEsquema(stmt, "google_cloud");
            crearEsquema(stmt, "azure_cloud");
            crearEsquema(stmt, "aws_cloud"); // Añadido esquema para AWS

            logger.info("Permisos y esquemas verificados con éxito");
        }
    }

    private void crearEsquema(Statement stmt, String schemaName) throws SQLException {
        logger.info("Verificando esquema '{}'...", schemaName);
        stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName + " AUTHORIZATION " + BD_USER);
        stmt.execute("GRANT ALL PRIVILEGES ON SCHEMA " + schemaName + " TO " + BD_USER);
        stmt.execute("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA " + schemaName + " TO " + BD_USER);
        stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA " + schemaName +
                " GRANT ALL PRIVILEGES ON TABLES TO " + BD_USER);
    }

    private void crearTablasConReintentos() throws SQLException {
        final int maxReintentos = 5;
        SQLException ultimoError = null;

        logger.info("Iniciando creación de tablas (máximo {} reintentos)", maxReintentos);

        for (int reintento = 1; reintento <= maxReintentos; reintento++) {
            try {
                logger.info("Intento {}/{} de creación de tablas", reintento, maxReintentos);

                CreacionTablasGoogle tablasGoogle = new CreacionTablasGoogle(BD_URL, BD_USER, BD_PASSWORD);
                CreacionTablasAzure tablasAzure = new CreacionTablasAzure(BD_URL, BD_USER, BD_PASSWORD);
                CreacionTablasAWS tablasAWS = new CreacionTablasAWS(BD_URL, BD_USER, BD_PASSWORD); // Añadida la creación de tablas AWS

                tablasGoogle.crearAllTablas();
                tablasAzure.crearAllTablas();
                tablasAWS.crearAllTablas();

                logger.info("Tablas creadas exitosamente en el intento {}", reintento);
                return;

            } catch (SQLException e) {
                ultimoError = e;
                logger.error("Error en intento {}/{}: {}", reintento, maxReintentos, e.getMessage());

                if (reintento < maxReintentos) {
                    esperarEntreReintentos(reintento);
                }
            }
        }

        throw new SQLException("Fallo al crear tablas después de " + maxReintentos + " intentos", ultimoError);
    }

    private void esperarEntreReintentos(int reintentoActual) throws SQLException {
        int tiempoEsperaSegundos = reintentoActual * 2;
        logger.info("Esperando {} segundos antes del próximo intento...", tiempoEsperaSegundos);

        try {
            Thread.sleep(tiempoEsperaSegundos * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupción durante la espera entre reintentos", e);
        }
    }
}


