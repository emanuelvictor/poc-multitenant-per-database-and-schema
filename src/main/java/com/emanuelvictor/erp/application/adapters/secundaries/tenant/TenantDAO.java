package com.emanuelvictor.erp.application.adapters.secundaries.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Busca os tenants de forma standAlone, sem setar tenant. Ou seja, sempre pega de 127.0.0.1:central/central.
 */
public final class TenantDAO {

    public static final Logger LOGGER = LoggerFactory.getLogger(TenantDAO.class);

    private static final HashMap<String, TenantDetails> ALL_TENANTS = new HashMap<>();
    public static final TenantDetails CENTRAL_TENANT = new TTenant("central", "central", "127.0.0.1", null);

    public static HashMap<String, TenantDetails> getAllCostumerTenants() {
        if (!ALL_TENANTS.isEmpty())
            return ALL_TENANTS;
        final List<TenantDetails> costumerTenants = new ArrayList<>();
        final Statement statement = openACentralStatement();
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery("SELECT * FROM central.tenant");
            while (resultSet.next()) {
                final TenantDetails tenant = extractTenantFromResultSetAndCreateDatasourceIfNotExists(resultSet);
                costumerTenants.add(tenant);
            }
            closeResultSet(resultSet);
        } catch (Exception e) {
            LOGGER.error("Error to get all tenants", e);
            closeResultSet(resultSet);
        }
        costumerTenants.forEach(tenant -> {
            ALL_TENANTS.put(tenant.getSchema(), tenant);
        });
        return ALL_TENANTS;
    }

    public static void addNewTenant(final TenantDetails tenantDetails) {
        final Statement statement = openACentralStatement();
        try {
            final String query = "INSERT INTO central.tenant(schema, database, address) " +
                    "VALUES('" + tenantDetails.getSchema() + "','" + tenantDetails.getDatabase() + "','" + tenantDetails.getAddress() + "')";
            statement.executeUpdate(query);
            closeStatement(statement);
        } catch (Exception e) {
            LOGGER.error("Error to add tenant {}", tenantDetails.getSchema(), e);
            closeStatement(statement);
            throw new RuntimeException(e);
        }
        ALL_TENANTS.put(tenantDetails.getSchema(), tenantDetails);
    }

    public static Integer getTotalOfDatabases() {
        final Statement statement = openACentralStatement();
        try {
            final String query = "SELECT COUNT(*) as total_bases " +
                    " FROM pg_database WHERE datname NOT IN ('postgres', 'template0', 'template1');";
            final ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();
            final Integer countOfConnections = resultSet.getInt("total_bases");
            closeResultSet(resultSet);
            return countOfConnections;
        } catch (Exception e) {
            LOGGER.error("Error to get count of databases", e);
            closeStatement(statement);
            throw new RuntimeException(e);
        }
    }

    public static Integer getTotalOfConnections() {
        final Statement statement = openACentralStatement();
        try {
            final String query = "SELECT count(*) FROM pg_stat_activity;";
            final ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();
            final Integer countOfConnections = resultSet.getInt("count");
            closeResultSet(resultSet);
            return countOfConnections;
        } catch (Exception e) {
            LOGGER.error("Error to get count of connections", e);
            closeStatement(statement);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new database from param, if not exists.
     *
     * @param database String
     */
    public static void createNewDatabase(String database) {
        try {
            final Connection connection = CENTRAL_TENANT.getDataSource().getConnection();
            final Statement statement = connection.createStatement();
            try {
                statement.executeUpdate("CREATE DATABASE " + database);
                LOGGER.info("Success to create database {}", database);
                connection.close();
                statement.close();
            } catch (Exception e) {
                connection.close();
                statement.close();
                if (e.getMessage().toLowerCase().contains("already exists")) { // by pass when database already exists. When database has already created, use it.
                    LOGGER.info("Database {} already exists. We cant create it", database);
                } else
                    throw new RuntimeException(e);
            }
        } catch (Exception e) {
            LOGGER.error("Error to open connection to create database {}", database, e);
            throw new RuntimeException(e);
        }
    }

    public static void dropAllTenants() {
        ALL_TENANTS.forEach((schema, tenantDetails) -> {
            dropSchemaOrDatabaseFromTenantDetails(tenantDetails);
        });
        ALL_TENANTS.clear();
        dropSchemaOrDatabaseFromTenantDetails(CENTRAL_TENANT);
    }

    private static void dropSchemaOrDatabaseFromTenantDetails(final TenantDetails tenantDetails) {
        final Statement statement = openACentralStatement();
        try {
            final String query;
            if (!tenantDetails.getDatabase().equals(CENTRAL_TENANT.getDatabase()))
                query = "DROP DATABASE IF EXISTS " + tenantDetails.getDatabase() + " WITH (FORCE)";
            else
                query = "DROP SCHEMA IF EXISTS " + tenantDetails.getSchema() + " CASCADE";
            statement.executeUpdate(query);
            closeStatement(statement);
        } catch (Exception e) {
            LOGGER.error("Error to drop tenant {}", tenantDetails.getSchema(), e);
            closeStatement(statement);
            throw new RuntimeException(e);
        }
    }

    private static TenantDetails extractTenantFromResultSetAndCreateDatasourceIfNotExists(ResultSet resultSet) {
        try {
            final String schema = resultSet.getString("schema");
            final String database = resultSet.getString("database");
            final String address = resultSet.getString("address");
            final DataSource dataSource;

            if (database.equals("central")) {
                dataSource = CENTRAL_TENANT.getDataSource();
                return new TTenant(schema, database, address, dataSource);
            }

            final TenantDetails tenantDetails = new TTenant(schema, database, address, null);
            dataSource = tenantDetails.getDataSource();
            ALL_TENANTS.put(tenantDetails.getSchema(), tenantDetails);
            return new TTenant(schema, database, address, dataSource);
        } catch (Exception e) {
            LOGGER.info("Error to convert ResultSet to Tenant", e);
            throw new RuntimeException(e);
        }
    }

    private static Statement openACentralStatement() {
        try {
            final Connection connection = CENTRAL_TENANT.getDataSource().getConnection();
            return connection.createStatement();
        } catch (Exception e) {
            LOGGER.error("Error to open statement to central tenant", e);
            throw new RuntimeException(e);
        }
    }

    private static void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
            assert resultSet != null;
            closeStatement(resultSet.getStatement());
        } catch (Exception e) {
            LOGGER.error("Error to close resultSet ", e);
            throw new RuntimeException(e);
        }
    }

    private static void closeStatement(Statement statement) {
        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            assert statement != null;
            if (statement.getConnection() != null && !statement.getConnection().isClosed()) {
                statement.getConnection().close();
            }
        } catch (Exception e) {
            LOGGER.error("Error to close statement ", e);
            throw new RuntimeException(e);
        }
    }
}
