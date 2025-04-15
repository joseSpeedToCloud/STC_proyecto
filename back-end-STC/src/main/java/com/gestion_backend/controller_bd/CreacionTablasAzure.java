package com.gestion_backend.controller_bd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreacionTablasAzure {
    private static final Logger logger = LoggerFactory.getLogger(CreacionTablasAzure.class);

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public CreacionTablasAzure(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        logger.info("Inicializando CreacionTablasAzure con URL: {}", dbUrl);
    }

    public void crearAllTablas() throws SQLException {
        logger.info("Iniciando creación de todas las tablas de Azure Cloud");
        try {
            crearSchemas();
            crearTablaDirectorios();
            crearTablaSuscripciones();
            crearTablaGruposRecursos();
            crearTablaMaquinasVirtuales();
            crearTablaRedesVirtuales();
            crearTablaSubredes();
            crearTablaDiscos();
            crearTablaSnapshots();
            crearTablaLoadBalancers();
            crearTablaVPNGateways();
            crearTablaGruposSeguridad();
            crearTablaAKSClusters();
            logger.info("Todas las tablas de Azure Cloud fueron creadas correctamente");
        } catch (SQLException e) {
            logger.error("Error al crear tablas de Azure Cloud: " + e.getMessage(), e);
            throw e;
        }
    }

    private void crearSchemas() throws SQLException {
        logger.info("Creando schema azure_cloud si no existe");
        try {
            // Verificar conexión primero
            try (Connection testConn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                logger.info("Conexión a la base de datos verificada para Azure");
            }

            // Crear schema con más privilegios
            executeSQL("CREATE SCHEMA IF NOT EXISTS azure_cloud AUTHORIZATION " + dbUser);
            executeSQL("GRANT ALL PRIVILEGES ON SCHEMA azure_cloud TO " + dbUser);
            executeSQL("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA azure_cloud TO " + dbUser);

            logger.info("Schema azure_cloud creado/verificado con permisos");
        } catch (SQLException e) {
            logger.error("Error crítico creando schema azure_cloud: {}", e.getMessage());

            // Intentar solución más agresiva
            try {
                executeSQL("DROP SCHEMA IF EXISTS azure_cloud CASCADE");
                executeSQL("CREATE SCHEMA azure_cloud AUTHORIZATION " + dbUser);
                executeSQL("GRANT ALL PRIVILEGES ON SCHEMA azure_cloud TO " + dbUser);
                logger.info("Schema azure_cloud recreado exitosamente");
            } catch (SQLException recreateEx) {
                logger.error("Fallo catastrófico recreando schema azure_cloud: {}", recreateEx.getMessage());
                throw new SQLException("No se pudo crear el schema azure_cloud después de varios intentos", recreateEx);
            }
        }
    }

    private void crearTablaDirectorios() throws SQLException {
        logger.info("Creando tabla azure_cloud.directories si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.directories (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE)";
        executeSQLWithRetry(sql, "azure_cloud.directories");
    }

    private void crearTablaSuscripciones() throws SQLException {
        logger.info("Creando tabla azure_cloud.subscriptions si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.subscriptions (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE, " +
                "display_name VARCHAR(255), " +
                "directory_id INTEGER REFERENCES azure_cloud.directories(id))";
        executeSQLWithRetry(sql, "azure_cloud.subscriptions");
    }

    private void crearTablaGruposRecursos() throws SQLException {
        logger.info("Creando tabla azure_cloud.resource_groups si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.resource_groups (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "location VARCHAR(255), " +
                "provisioning_state VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.resource_groups");
    }

    private void crearTablaMaquinasVirtuales() throws SQLException {
        logger.info("Creando tabla azure_cloud.virtual_machines si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.virtual_machines (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "resource_group_id VARCHAR(255) REFERENCES azure_cloud.resource_groups(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "vm_size VARCHAR(255), " +
                "location VARCHAR(255), " +
                "status VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.virtual_machines");
    }

    private void crearTablaRedesVirtuales() throws SQLException {
        logger.info("Creando tabla azure_cloud.virtual_networks si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.virtual_networks (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "resource_group_id VARCHAR(255) REFERENCES azure_cloud.resource_groups(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "address_space VARCHAR(255), " +
                "enable_ddos_protection BOOLEAN, " +
                "enable_vm_protection BOOLEAN, " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.virtual_networks");
    }

    private void crearTablaSubredes() throws SQLException {
        logger.info("Creando tabla azure_cloud.subnets si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.subnets (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "virtual_network_id VARCHAR(255) REFERENCES azure_cloud.virtual_networks(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "address_prefix VARCHAR(255), " +
                "private_endpoint_network_policies VARCHAR(50), " +
                "private_link_service_network_policies VARCHAR(50), " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.subnets");
    }

    private void crearTablaDiscos() throws SQLException {
        logger.info("Creando tabla azure_cloud.disks si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.disks (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "resource_group_id VARCHAR(255) REFERENCES azure_cloud.resource_groups(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "disk_type VARCHAR(255), " +
                "location VARCHAR(255), " +
                "size_gb BIGINT, " +
                "status VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.disks");
    }

    private void crearTablaSnapshots() throws SQLException {
        logger.info("Creando tabla azure_cloud.snapshots si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.snapshots (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "resource_group_id VARCHAR(255) REFERENCES azure_cloud.resource_groups(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "source_disk_id VARCHAR(255), " +
                "disk_size_gb BIGINT, " +
                "status VARCHAR(50), " +
                "storage_account_type VARCHAR(255), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.snapshots");
    }

    private void crearTablaLoadBalancers() throws SQLException {
        logger.info("Creando tabla azure_cloud.load_balancers si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.load_balancers (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "resource_group_id VARCHAR(255) REFERENCES azure_cloud.resource_groups(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "location VARCHAR(255), " +
                "sku VARCHAR(50), " +
                "frontend_ip_configuration VARCHAR(255), " +
                "backend_address_pools VARCHAR(255), " +
                "probe_protocol VARCHAR(50), " +
                "probe_port INTEGER, " +
                "status VARCHAR(50), " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.load_balancers");
    }

    private void crearTablaVPNGateways() throws SQLException {
        logger.info("Creando tabla azure_cloud.vpn_gateways si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.vpn_gateways (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "resource_group_id VARCHAR(255) REFERENCES azure_cloud.resource_groups(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "location VARCHAR(255), " +
                "type VARCHAR(50), " +
                "status VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.vpn_gateways");
    }

    private void crearTablaGruposSeguridad() throws SQLException {
        logger.info("Creando tabla azure_cloud.network_security_groups si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.network_security_groups (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "resource_group_id VARCHAR(255) REFERENCES azure_cloud.resource_groups(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "location VARCHAR(255), " +
                "security_rules JSONB, " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.network_security_groups");
    }

    private void crearTablaAKSClusters() throws SQLException {
        logger.info("Creando tabla azure_cloud.aks_clusters si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS azure_cloud.aks_clusters (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "resource_group_id VARCHAR(255) REFERENCES azure_cloud.resource_groups(id), " +
                "subscription_id VARCHAR(255) REFERENCES azure_cloud.subscriptions(id), " +
                "location VARCHAR(255), " +
                "kubernetes_version VARCHAR(50), " +
                "status VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, subscription_id))";
        executeSQLWithRetry(sql, "azure_cloud.aks_clusters");
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

