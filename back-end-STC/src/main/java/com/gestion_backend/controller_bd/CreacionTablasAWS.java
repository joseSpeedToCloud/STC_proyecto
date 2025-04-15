package com.gestion_backend.controller_bd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreacionTablasAWS {
    private static final Logger logger = LoggerFactory.getLogger(CreacionTablasAWS.class);

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public CreacionTablasAWS(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        logger.info("Inicializando CreacionTablasAWS con URL: {}", dbUrl);
    }

    public void crearAllTablas() throws SQLException {
        logger.info("Iniciando creación de todas las tablas de AWS");
        try {
            crearSchemas();
            crearTablaOrganizaciones();
            crearTablaCuentas();
            crearTablaEC2Instances();
            crearTablaVPC();
            crearTablaSubredes();
            crearTablaEBS();
            crearTablaSnapshots();
            crearTablaLoadBalancers();
            crearTablaVPNConnections();
            crearTablaSecurityGroups();
            crearTablaEKSClusters();
            crearTablaIAMRoles();
            crearTablaS3Buckets();
            crearTablaRDSInstances();
            logger.info("Todas las tablas de AWS fueron creadas correctamente");
        } catch (SQLException e) {
            logger.error("Error al crear tablas de AWS: " + e.getMessage(), e);
            throw e;
        }
    }

    private void crearSchemas() throws SQLException {
        logger.info("Creando schema aws_cloud si no existe");
        try {
            // Verificar conexión primero
            try (Connection testConn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                logger.info("Conexión a la base de datos verificada para AWS");
            }

            // Crear schema con más privilegios
            executeSQL("CREATE SCHEMA IF NOT EXISTS aws_cloud AUTHORIZATION " + dbUser);
            executeSQL("GRANT ALL PRIVILEGES ON SCHEMA aws_cloud TO " + dbUser);
            executeSQL("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA aws_cloud TO " + dbUser);

            logger.info("Schema aws_cloud creado/verificado con permisos");
        } catch (SQLException e) {
            logger.error("Error crítico creando schema aws_cloud: {}", e.getMessage());

            // Intentar solución más agresiva
            try {
                executeSQL("DROP SCHEMA IF EXISTS aws_cloud CASCADE");
                executeSQL("CREATE SCHEMA aws_cloud AUTHORIZATION " + dbUser);
                executeSQL("GRANT ALL PRIVILEGES ON SCHEMA aws_cloud TO " + dbUser);
                logger.info("Schema aws_cloud recreado exitosamente");
            } catch (SQLException recreateEx) {
                logger.error("Fallo catastrófico recreando schema aws_cloud: {}", recreateEx.getMessage());
                throw new SQLException("No se pudo crear el schema aws_cloud después de varios intentos", recreateEx);
            }
        }
    }

    private void crearTablaOrganizaciones() throws SQLException {
        logger.info("Creando tabla aws_cloud.organizations si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.organizations (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE, " +
                "aws_org_id VARCHAR(255) UNIQUE)";
        executeSQLWithRetry(sql, "aws_cloud.organizations");
    }

    private void crearTablaCuentas() throws SQLException {
        logger.info("Creando tabla aws_cloud.accounts si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.accounts (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) UNIQUE, " +
                "organization_id INTEGER REFERENCES aws_cloud.organizations(id), " +
                "status VARCHAR(50), " +
                "details JSONB)";
        executeSQLWithRetry(sql, "aws_cloud.accounts");
    }

    private void crearTablaEC2Instances() throws SQLException {
        logger.info("Creando tabla aws_cloud.ec2_instances si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.ec2_instances (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255), " +
                "instance_id VARCHAR(255) NOT NULL, " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "instance_type VARCHAR(255), " +
                "availability_zone VARCHAR(255), " +
                "state VARCHAR(50), " +
                "public_ip VARCHAR(255), " +
                "private_ip VARCHAR(255), " +
                "details JSONB, " +
                "UNIQUE (instance_id, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.ec2_instances");
    }

    private void crearTablaVPC() throws SQLException {
        logger.info("Creando tabla aws_cloud.vpcs si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.vpcs (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "vpc_id VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255), " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "cidr_block VARCHAR(255), " +
                "is_default BOOLEAN, " +
                "region VARCHAR(255), " +
                "tenancy VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (vpc_id, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.vpcs");
    }

    private void crearTablaSubredes() throws SQLException {
        logger.info("Creando tabla aws_cloud.subnets si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.subnets (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "subnet_id VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255), " +
                "vpc_id VARCHAR(255) REFERENCES aws_cloud.vpcs(id), " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "cidr_block VARCHAR(255), " +
                "availability_zone VARCHAR(255), " +
                "map_public_ip BOOLEAN, " +
                "details JSONB, " +
                "UNIQUE (subnet_id, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.subnets");
    }

    private void crearTablaEBS() throws SQLException {
        logger.info("Creando tabla aws_cloud.ebs_volumes si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.ebs_volumes (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "volume_id VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255), " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "type VARCHAR(50), " +
                "size_gb INTEGER, " +
                "state VARCHAR(50), " +
                "availability_zone VARCHAR(255), " +
                "encrypted BOOLEAN, " +
                "details JSONB, " +
                "UNIQUE (volume_id, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.ebs_volumes");
    }

    private void crearTablaSnapshots() throws SQLException {
        logger.info("Creando tabla aws_cloud.snapshots si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.snapshots (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "snapshot_id VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255), " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "volume_id VARCHAR(255), " +
                "volume_size INTEGER, " +
                "state VARCHAR(50), " +
                "encrypted BOOLEAN, " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (snapshot_id, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.snapshots");
    }

    private void crearTablaLoadBalancers() throws SQLException {
        logger.info("Creando tabla aws_cloud.load_balancers si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.load_balancers (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "arn VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255) NOT NULL, " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "region VARCHAR(255), " +
                "type VARCHAR(50), " +
                "scheme VARCHAR(50), " +
                "vpc_id VARCHAR(255) REFERENCES aws_cloud.vpcs(id), " +
                "dns_name VARCHAR(255), " +
                "state VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (name, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.load_balancers");
    }

    private void crearTablaVPNConnections() throws SQLException {
        logger.info("Creando tabla aws_cloud.vpn_connections si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.vpn_connections (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "vpn_connection_id VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255), " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "vpc_id VARCHAR(255) REFERENCES aws_cloud.vpcs(id), " +
                "state VARCHAR(50), " +
                "customer_gateway_id VARCHAR(255), " +
                "vpn_gateway_id VARCHAR(255), " +
                "type VARCHAR(50), " +
                "details JSONB, " +
                "UNIQUE (vpn_connection_id, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.vpn_connections");
    }

    private void crearTablaSecurityGroups() throws SQLException {
        logger.info("Creando tabla aws_cloud.security_groups si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.security_groups (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "group_id VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255) NOT NULL, " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "vpc_id VARCHAR(255) REFERENCES aws_cloud.vpcs(id), " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (group_id, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.security_groups");
    }

    private void crearTablaEKSClusters() throws SQLException {
        logger.info("Creando tabla aws_cloud.eks_clusters si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.eks_clusters (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "region VARCHAR(255), " +
                "status VARCHAR(50), " +
                "version VARCHAR(50), " +
                "role_arn VARCHAR(255), " +
                "vpc_id VARCHAR(255) REFERENCES aws_cloud.vpcs(id), " +
                "details JSONB, " +
                "UNIQUE (name, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.eks_clusters");
    }

    private void crearTablaIAMRoles() throws SQLException {
        logger.info("Creando tabla aws_cloud.iam_roles si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.iam_roles (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "arn VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255) NOT NULL, " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "path VARCHAR(255), " +
                "create_date TIMESTAMP, " +
                "description VARCHAR(500), " +
                "details JSONB, " +
                "UNIQUE (name, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.iam_roles");
    }

    private void crearTablaS3Buckets() throws SQLException {
        logger.info("Creando tabla aws_cloud.s3_buckets si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.s3_buckets (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "region VARCHAR(255), " +
                "creation_date TIMESTAMP, " +
                "versioning_enabled BOOLEAN, " +
                "public_access_blocked BOOLEAN, " +
                "details JSONB, " +
                "UNIQUE (name))";
        executeSQLWithRetry(sql, "aws_cloud.s3_buckets");
    }

    private void crearTablaRDSInstances() throws SQLException {
        logger.info("Creando tabla aws_cloud.rds_instances si no existe");
        String sql = "CREATE TABLE IF NOT EXISTS aws_cloud.rds_instances (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "db_instance_identifier VARCHAR(255) NOT NULL, " +
                "account_id VARCHAR(255) REFERENCES aws_cloud.accounts(id), " +
                "engine VARCHAR(255), " +
                "status VARCHAR(50), " +
                "instance_class VARCHAR(255), " +
                "allocated_storage INTEGER, " +
                "vpc_id VARCHAR(255) REFERENCES aws_cloud.vpcs(id), " +
                "endpoint VARCHAR(255), " +
                "multi_az BOOLEAN, " +
                "details JSONB, " +
                "UNIQUE (db_instance_identifier, account_id))";
        executeSQLWithRetry(sql, "aws_cloud.rds_instances");
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

