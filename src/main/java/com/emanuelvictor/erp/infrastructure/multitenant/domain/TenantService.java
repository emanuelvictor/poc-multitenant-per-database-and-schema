package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Busca os tenants de forma standAlone, sem setar tenant. Ou seja, sempre pega de 127.0.0.1:central/public.
 */
public final class TenantService {

    public static final Logger LOGGER = LoggerFactory.getLogger(TenantService.class);

    public static final TenantDetails CENTRAL_DATA_SOURCE = new TTenant("public", "central", "127.0.0.1", null);
    // DATASOURCES de databases, tenants do central que se diferenciam por esqeuma não estão aqui
    public static final HashMap<Object, TenantDetails> CLIENT_DATA_SOURCES = new HashMap<>();
    public static final HashMap<Object, TenantDetails> ALL_TENANTS = new HashMap<>();

    public static HashMap<Object, TenantDetails> getAllCostumerTenants() {
        if (!CLIENT_DATA_SOURCES.isEmpty()) {
            return CLIENT_DATA_SOURCES;
        }
        final List<TenantDetails> costumerTenants = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = CENTRAL_DATA_SOURCE.getDataSource().getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM public.tenant");
            while (resultSet.next()) {
                final TenantDetails tenant = getTenantFromResultSet(resultSet);
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

    // TODo talvez não haja necessidade
    public static HashMap<Object, TenantDetails> getCostumerTenantsWithDatabaseDifferentOfCentral() {
        ALL_TENANTS.forEach((tenant, tenantDetails) -> {
            if (!tenantDetails.getDatabase().equals(CENTRAL_DATA_SOURCE.getDatabase())) {
                CLIENT_DATA_SOURCES.put(tenant, tenantDetails);
            }
        });
        return CLIENT_DATA_SOURCES;
    }

    public static void addNewTenant(final TenantDetails tenantDetails) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = CENTRAL_DATA_SOURCE.getDataSource().getConnection();
            statement = connection.createStatement();
            final String query = "INSERT INTO public.tenant(schema, database, address) " +
                    "VALUES('" + tenantDetails.getSchema() + "','" + tenantDetails.getDatabase() + "','" + tenantDetails.getAddress() + "')";
            System.out.println(query);
            statement.executeUpdate(query);
            connection.close();
            statement.close();
            CLIENT_DATA_SOURCES.put(tenantDetails.getSchema(), tenantDetails);
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

    private static TenantDetails getTenantFromResultSet(ResultSet resultSet) {
        try {
            final TenantDetails tenant = CLIENT_DATA_SOURCES.get(resultSet.getString("schema"));
            final DataSource dataSource = Objects.requireNonNullElse(tenant, CENTRAL_DATA_SOURCE).getDataSource();
            return new TTenant(resultSet.getString("schema"), resultSet.getString("database"), resultSet.getString("address"), dataSource);
        } catch (Exception e) {
            LOGGER.info("Error to convert ResultSet to Tenant", e);
            throw new RuntimeException(e);
        }
    }
}
