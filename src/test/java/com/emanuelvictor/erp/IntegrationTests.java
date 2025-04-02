package com.emanuelvictor.erp;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;

import static com.emanuelvictor.erp.infrastructure.multitenant.TenantDAO.CENTRAL_TENANT;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(HandlerCamelCaseNameFromClasses.class)
public abstract class IntegrationTests {

    public static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTests.class);

    @Autowired
    protected MockMvc mockMvc;

    static {
        final GenericContainer<?> postgres = new GenericContainer<>(DockerImageName.parse("postgres:13.2-alpine"))
                .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
                .withEnv("POSTGRES_USER", "central")
                .withEnv("POSTGRES_PASSWORD", "central")
                .withEnv("POSTGRES_DB", "central");
        postgres.setPortBindings(Collections.singletonList(String.format("%d:%d/%s", 5434, 5432, InternetProtocol.TCP.toDockerNotation())));
        postgres.start();
        migrateCentralTenant();
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

}