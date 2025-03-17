package com.emanuelvictor.erp.infrastructure.multitenant;

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
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = CENTRAL_TENANT.getDataSource().getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM central.tenant");
            while (resultSet.next()) {
                final TenantDetails tenant = extractTenantFromResultSetAndCreateDatasourceIfNotExists(resultSet);
                costumerTenants.add(tenant);
            }
            resultSet.close();
            connection.close();
            statement.close();
        } catch (Exception e) {
            LOGGER.error("Error to get all tenants", e);
            try {
                if (resultSet != null && !resultSet.isClosed()) {
                    resultSet.close();
                }
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
            } catch (Exception e1) {
                LOGGER.error("Error to close connection", e1);
            }
        }
        costumerTenants.forEach(tenant -> {
            ALL_TENANTS.put(tenant.getSchema(), tenant);
        });
        return ALL_TENANTS;
    }

    public static void addNewTenant(final TenantDetails tenantDetails) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = CENTRAL_TENANT.getDataSource().getConnection();
            statement = connection.createStatement();
            final String query = "INSERT INTO central.tenant(schema, database, address) " +
                    "VALUES('" + tenantDetails.getSchema() + "','" + tenantDetails.getDatabase() + "','" + tenantDetails.getAddress() + "')";
            statement.executeUpdate(query);
            connection.close();
            statement.close();
            ALL_TENANTS.put(tenantDetails.getSchema(), tenantDetails);
        } catch (Exception e) {
            LOGGER.error("Error to add tenant {}", tenantDetails.getSchema(), e);
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
            } catch (Exception e1) {
                LOGGER.error("Error to close connection", e1);
            }
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
}
