package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import com.emanuelvictor.erp.infrastructure.multitenant.TenantIdentifierResolver;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;

import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantService.*;
import static java.util.Optional.ofNullable;

@Service
public class TenantMigrationService extends AbstractRoutingDataSource {

    public static final Logger LOGGER = LoggerFactory.getLogger(TenantMigrationService.class);

    private final TenantIdentifierResolver tenantIdentifierResolver;

    public TenantMigrationService(TenantIdentifierResolver tenantIdentifierResolver) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;

        configureRoutingDataSources();
        migrateCentralTenant();
        migrateAllCostumerTenants();
    }

    public void configureRoutingDataSources() {
        setDefaultTargetDataSource(CENTRAL_DATA_SOURCE.getDataSource());

        final HashMap<Object, TenantDetails> costumerTenants = getCostumerTenantsWithDatabaseDifferentOfCentral(); // TODO talvez funcione com o getAllCostumerTenants e esse método possa ser deletado
        final HashMap<Object, Object> targetDataSources = new HashMap<>();
        costumerTenants.forEach((schema, tenantDetails) ->
                targetDataSources.put(schema, tenantDetails.getDataSource()));
        setTargetDataSources(targetDataSources);

        initialize();
    }

    /**
     * Migrate our tenant (central tenant). Database central and schema public
     */
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

    /**
     * Migrate all costumer tenants. With dedicated databases or not.
     */
    public void migrateAllCostumerTenants() {
        getAllCostumerTenants().forEach((_schema, tenantDetails) -> migrate(tenantDetails));
    }

    public void migrate(TenantDetails tenant) {
        try {
            tryToCreateNewDatabase(tenant.getDatabase()); // TODO esse try não vai rolar, tem que fazer um if exists database
            final Flyway flyway = new FluentConfiguration().dataSource(tenant.getDataSource())
                    .schemas(tenant.getSchema()).baselineOnMigrate(true).locations("db/migration").load();
            flyway.migrate();
        } catch (final Exception e) {
            LOGGER.error("Error while migrating tenant {}", tenant.getSchema(), e);
            throw new RuntimeException(e);
        }
    }

    // TODO verificar possibilidade de ir para o TenantService
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

    @Override
    protected String determineCurrentLookupKey() {
        final TenantDetails tenantDetails =
                ofNullable(CLIENT_DATA_SOURCES.get(tenantIdentifierResolver.resolveCurrentTenantIdentifier()))
                        .orElse(CENTRAL_DATA_SOURCE);
        return tenantDetails.getDatabase();
    }
}
