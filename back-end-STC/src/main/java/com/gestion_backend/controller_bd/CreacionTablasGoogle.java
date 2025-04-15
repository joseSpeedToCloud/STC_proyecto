package com.gestion_backend.controller_bd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreacionTablasGoogle {
    private static final Logger logger = LoggerFactory.getLogger(CreacionTablasGoogle.class);

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public CreacionTablasGoogle(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        logger.info("Inicializando CreacionTablasGoogle con URL: {}", dbUrl);
    }

    public void crearAllTablas() throws SQLException {
        logger.info("Iniciando creación de todas las tablas de Google Cloud");
        try {
            crearSchemas();
            crearTablaOrganizaciones();
            crearTablaProyectos();
            crearTablaMaquinasVirtuales();
            crearTablaRedes();
            crearTablaSubredes();
            crearTablaDiscos();
            crearTablaSnapshots();
            crearTablaLoadBalancers();
            crearTablaVPNs();
            crearTablaFirewalls();
            crearTablaGKEClusters();
            crearTablaUsuarios();
            logger.info("Todas las tablas de Google Cloud fueron creadas correctamente");
        } catch (SQLException e) {
            logger.error("Error al crear tablas de Google Cloud: " + e.getMessage(), e);
            throw e;
        }
    }

    private void crearSchemas() throws SQLException {
        logger.info("Creando schema google_cloud si no existe");
        try {
            // Verificar conexión primero
            try (Connection testConn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                logger.info("Conexión a la base de datos verificada para Google Cloud");
            }

            // Crear schema con más privilegios
            executeSQL("CREATE SCHEMA IF NOT EXISTS google_cloud AUTHORIZATION " + dbUser);
            executeSQL("GRANT ALL PRIVILEGES ON SCHEMA google_cloud TO " + dbUser);
            executeSQL("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA google_cloud TO " + dbUser);

            logger.info("Schema google_cloud creado/verificado con permisos");
        } catch (SQLException e) {
            logger.error("Error crítico creando schema google_cloud: {}", e.getMessage());

            // Intentar solución más agresiva
            try {
                executeSQL("DROP SCHEMA IF EXISTS google_cloud CASCADE");
                executeSQL("CREATE SCHEMA google_cloud AUTHORIZATION " + dbUser);
                executeSQL("GRANT ALL PRIVILEGES ON SCHEMA google_cloud TO " + dbUser);
                logger.info("Schema google_cloud recreado exitosamente");
            } catch (SQLException recreateEx) {
                logger.error("Fallo catastrófico recreando schema google_cloud: {}", recreateEx.getMessage());
                throw new SQLException("No se pudo crear el schema google_cloud después de varios intentos", recreateEx);
            }
        }
    }

    private void crearTablaOrganizaciones() throws SQLException {
        logger.info("Creando tabla google_cloud.organizations si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.organizations (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE)";
        executeSQLWithRetry(sql, "google_cloud.organizations");
    }

    private void crearTablaProyectos() throws SQLException {
        logger.info("Creando tabla google_cloud.projects si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.projects (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE, " +
                "display_name VARCHAR(255), " +
                "organization_id INTEGER REFERENCES google_cloud.organizations(id))";
        executeSQLWithRetry(sql, "google_cloud.projects");
    }

    private void crearTablaMaquinasVirtuales() throws SQLException {
        logger.info("Creando tabla google_cloud.virtual_machines si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.virtual_machines (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "machine_type VARCHAR(255), " +
                "zone VARCHAR(255), " +
                "status VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.virtual_machines");
    }

    private void crearTablaRedes() throws SQLException {
        logger.info("Creando tabla google_cloud.networks si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.networks (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "auto_create_subnetworks BOOLEAN, " +
                "routing_mode VARCHAR(50), " +
                "mtu INTEGER, " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.networks");
    }

    private void crearTablaSubredes() throws SQLException {
        logger.info("Creando tabla google_cloud.subnetworks si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.subnetworks (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "network_id VARCHAR(255) REFERENCES google_cloud.networks(id), " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "region VARCHAR(255), " +
                "ip_cidr_range VARCHAR(255), " +
                "private_ip_google_access BOOLEAN, " +
                "stack_type VARCHAR(50), " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.subnetworks");
    }

    private void crearTablaDiscos() throws SQLException {
        logger.info("Creando tabla google_cloud.disks si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.disks (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "type VARCHAR(255), " +
                "zone VARCHAR(255), " +
                "size_gb BIGINT, " +
                "status VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.disks");
    }

    private void crearTablaSnapshots() throws SQLException {
        logger.info("Creando tabla google_cloud.snapshots si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.snapshots (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "source_disk VARCHAR(255), " +
                "disk_size_gb BIGINT, " +
                "status VARCHAR(50), " +
                "storage_locations VARCHAR(255), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.snapshots");
    }

    private void crearTablaLoadBalancers() throws SQLException {
        logger.info("Creando tabla google_cloud.load_balancers si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.load_balancers (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "region VARCHAR(255), " +
                "load_balancing_scheme VARCHAR(50), " +
                "network_tier VARCHAR(50), " +
                "type VARCHAR(50), " +
                "ip_address VARCHAR(255), " +
                "port INTEGER, " +
                "status VARCHAR(50), " +
                "target VARCHAR(255), " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.load_balancers");
    }

    private void crearTablaVPNs() throws SQLException {
        logger.info("Creando tabla google_cloud.vpns si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.vpns (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "region VARCHAR(255), " +
                "status VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.vpns");
    }

    private void crearTablaFirewalls() throws SQLException {
        logger.info("Creando tabla google_cloud.firewalls si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.firewalls (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "direction VARCHAR(50), " +
                "priority VARCHAR(50), " +
                "protocol VARCHAR(50), " +
                "source_ranges VARCHAR(255), " +
                "target_tags VARCHAR(255), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.firewalls");
    }

    private void crearTablaGKEClusters() throws SQLException {
        logger.info("Creando tabla google_cloud.gke_clusters si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS google_cloud.gke_clusters (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "project_id VARCHAR(255) REFERENCES google_cloud.projects(id), " +
                "zone VARCHAR(255), " +
                "status VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, project_id))";
        executeSQLWithRetry(sql, "google_cloud.gke_clusters");
    }

    private void crearTablaUsuarios() throws SQLException {
        logger.info("Creando tabla users si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL, " +
                "last_name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) UNIQUE NOT NULL, " +
                "department VARCHAR(255) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "password_changed BOOLEAN NOT NULL DEFAULT false, " +
                "rol VARCHAR(50) NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'VIEWER'))" +
                ")";
        executeSQLWithRetry(sql, "users");
    }

    private void executeSQL(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.error("Error ejecutando SQL: {}", sql);
            throw e;
        }
    }

    private void executeSQLWithRetry(String sql, String tableName) throws SQLException {
        int maxRetries = 3;
        SQLException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                executeSQL(sql);
                logger.info("Tabla {} creada/verificada correctamente (intento {})", tableName, attempt);
                return;
            } catch (SQLException e) {
                lastException = e;
                logger.warn("Error en intento {} para tabla {}: {}", attempt, tableName, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // Espera progresiva
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrupción durante el reintento", ie);
                    }
                }
            }
        }

        throw new SQLException("No se pudo crear/verificar la tabla " + tableName + " después de " + maxRetries + " intentos", lastException);
    }
}

