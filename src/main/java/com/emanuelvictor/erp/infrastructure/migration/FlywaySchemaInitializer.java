package com.emanuelvictor.erp.infrastructure.migration;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantDetails;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantTableRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static com.emanuelvictor.erp.infrastructure.multitenant.TenantRoutingDatasource.dataSourceFromTenant;
import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantService.getCentralTenant;

@Configuration
@RequiredArgsConstructor
public class FlywaySchemaInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(FlywaySchemaInitializer.class);

    private final TenantTableRepository tenantTableRepository;

    @PostConstruct
    public void migrateAllTenants() {
        migrateCentralTenant();
        migrateCostumerTenants();
    }

    public void migrateCentralTenant() {
        migrate(getCentralTenant());
    }

    public void migrateCostumerTenants() {
        tenantTableRepository.findAll().forEach(this::migrate);
    }

    /**
     * Migrate a specific tenant.
     *
     * @param tenant {@link TenantDetails}
     */
    public void migrate(final TenantDetails tenant) {
        try {
            if (!tenant.getDatabase().equals("central")) {
                tryToCreateNewDatabase(tenant.getDatabase());
            }
            new FluentConfiguration().dataSource(dataSourceFromTenant(tenant))
                    .schemas(tenant.getSchema()).baselineOnMigrate(true).locations("db/migration").load().migrate();
        } catch (final Exception e) {
            LOGGER.error("Error while migrating tenant {}", tenant.getSchema(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * try to create a new database if it not exists
     *
     * @param database {@link String}
     */
    public static void tryToCreateNewDatabase(String database) {
        String url = "jdbc:postgresql://localhost:5432/central"; // TODO put in another location
        String user = "central"; // TODO put in another location
        String password = "central"; // TODO put in another location

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE " + database);
            LOGGER.info("Success to create database {}", database);
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("already exists"))
                LOGGER.info("Database {} already exists. We cant create it", database);
            else
                throw new RuntimeException(e);
        }
    }
}
