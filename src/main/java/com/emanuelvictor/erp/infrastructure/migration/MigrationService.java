package com.emanuelvictor.erp.infrastructure.migration;

import com.emanuelvictor.erp.infrastructure.multitenant.TenantDetails;
import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.emanuelvictor.erp.infrastructure.multitenant.TenantDAO.*;

@Service
public class MigrationService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MigrationService.class);

    @PostConstruct
    private void postConstruct() {
        migrateCentralTenant();
        migrateAllCostumerTenants();
    }

    /**
     * Migrate our tenant (central tenant). Database central and schema public
     */
    private static void migrateCentralTenant() {
        try {
            final Flyway flyway = new FluentConfiguration().dataSource(CENTRAL_TENANT.getDataSource())
                    .schemas(CENTRAL_TENANT.getSchema()).baselineOnMigrate(true).locations("db/migration").load();
            flyway.migrate();
        } catch (final Exception e) {
            LOGGER.error("Error while migrating tenant {}", CENTRAL_TENANT.getSchema(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Migrate all costumer tenants. With dedicated databases or not.
     */
    private static void migrateAllCostumerTenants() {
        getAllCostumerTenants().forEach((_schema, tenantDetails) -> migrate(tenantDetails));
    }

    /**
     * @param tenant {@link TenantDetails}
     */
    public static void migrate(TenantDetails tenant) {
        try {
            createNewDatabase(tenant.getDatabase());
            final Flyway flyway = new FluentConfiguration().dataSource(tenant.getDataSource())
                    .schemas(tenant.getSchema()).baselineOnMigrate(true).locations("db/migration").load();
            flyway.migrate();
        } catch (final Exception e) {
            LOGGER.error("Error while migrating tenant {}", tenant.getSchema(), e);
            throw new RuntimeException(e);
        }
    }
}
