package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import com.emanuelvictor.erp.infrastructure.multitenant.TenantIdentifierResolver;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
public class TenantMigrationService extends AbstractRoutingDataSource {

    public static final Logger LOGGER = LoggerFactory.getLogger(TenantMigrationService.class);

    public static final TenantDetails CENTRAL_DATA_SOURCE = new TTenant("public", "central", "127.0.0.1", null);
    // DATASOURCES de databases, tenants do central que se diferenciam por esqeuma não estão aqui
    public static final HashMap<String, TenantDetails> CLIENT_DATA_SOURCES = new HashMap<>();

    private final TenantIdentifierResolver tenantIdentifierResolver;

    public TenantMigrationService(TenantIdentifierResolver tenantIdentifierResolver) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;

        configureRoutingDataSource();
        migrateCentralTenant();
        migrateCostumerTenants();
    }

    void configureRoutingDataSource() {
        setDefaultTargetDataSource(CENTRAL_DATA_SOURCE.getDataSource());

        final List<TTenant> costumerTenants = getCostumerTenantsWithDatabaseDifferentOfCentral(); // TODO tem que puxar da memória também
        final HashMap<Object, Object> targetDataSources = new HashMap<>();
        costumerTenants.forEach(tenant -> {
            CLIENT_DATA_SOURCES.put(tenant.getSchema(), tenant);
            targetDataSources.put(tenant.getSchema(), tenant.getDataSource());
        });
        setTargetDataSources(targetDataSources);
    }

    private static void migrateCentralTenant() {
        try {
            final Flyway flyway = new FluentConfiguration().dataSource(CENTRAL_DATA_SOURCE.getDataSource())
                    .schemas(CENTRAL_DATA_SOURCE.getSchema()).baselineOnMigrate(true).locations("db/migration").load();
            flyway.migrate();
        } catch (final Exception e) {
            LOGGER.error("Error while migrating tenant {}", CENTRAL_DATA_SOURCE.getSchema(), e);
            throw new RuntimeException(e);
        }
    }

    public void migrateCostumerTenants() {
        getAllCostumerTenants().forEach(this::migrate);
    }

    public void migrate(TenantDetails tenant) {
        try {
            tryToCreateNewDatabase(tenant.getDatabase());
            final Flyway flyway = new FluentConfiguration().dataSource(tenant.getDataSource())
                    .schemas(tenant.getSchema()).baselineOnMigrate(true).locations("db/migration").load();
            flyway.migrate();
        } catch (final Exception e) {
            LOGGER.error("Error while migrating tenant {}", tenant.getSchema(), e);
            throw new RuntimeException(e);
        }
    }

    public static List<TTenant> getAllCostumerTenants() {
        final List<TTenant> costumerTenants = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = CENTRAL_DATA_SOURCE.getDataSource().getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM public.tenant");
            while (resultSet.next()) {
                final TTenant tenant = getTenantFromResultSet(resultSet);
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
        return costumerTenants;
    }

    public static List<TTenant> getCostumerTenantsWithDatabaseDifferentOfCentral() {
        return getAllCostumerTenants().stream().filter(tenant -> !tenant.getDatabase().equals(CENTRAL_DATA_SOURCE.getDatabase())).collect(Collectors.toList());
    }

    private static void tryToCreateNewDatabase(String database) {
        try {
            final Connection connection = CENTRAL_DATA_SOURCE.getDataSource().getConnection();
            final Statement statement = connection.createStatement();
            try {
                statement.executeUpdate("CREATE DATABASE " + database);
                LOGGER.info("Success to create database {}", database);
                connection.close();
                statement.close();
            } catch (Exception e) {
                connection.close();
                statement.close();
                if (e.getMessage().toLowerCase().contains("already exists")) {
                    LOGGER.info("Database {} already exists. We cant create it", database);
                } else
                    throw new RuntimeException(e);
            }
        } catch (Exception e) {
            LOGGER.error("Error to open connection to create database {}", database, e);
        }
    }

    private static TTenant getTenantFromResultSet(ResultSet resultSet) {
        try {
            final TenantDetails tenant = CLIENT_DATA_SOURCES.get(resultSet.getString("schema"));
            final DataSource dataSource;
            dataSource = Objects.requireNonNullElse(tenant, CENTRAL_DATA_SOURCE).getDataSource();
            return new TTenant(resultSet.getString("schema"), resultSet.getString("database"), resultSet.getString("address"), dataSource);
        } catch (Exception e) {
            LOGGER.info("Error to convert ResultSet to Tenant", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String determineCurrentLookupKey() {
        return ofNullable(CLIENT_DATA_SOURCES.get(tenantIdentifierResolver.resolveCurrentTenantIdentifier())).orElse(CENTRAL_DATA_SOURCE).getDatabase();
    }

    public void add(TenantDetails tenantDetails) {
        CLIENT_DATA_SOURCES.put(tenantDetails.getSchema(), tenantDetails);
        final HashMap<Object, Object> targetDataSources = new HashMap<>();
        CLIENT_DATA_SOURCES.forEach((schema, tenant) -> {
            targetDataSources.put(schema, tenant.getDataSource());
        });
        setTargetDataSources(targetDataSources);

        this.initialize();
    }
}
