package com.emanuelvictor.erp;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
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

import java.lang.reflect.Method;
import java.util.Collections;

import static com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO.CENTRAL_TENANT;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(AbstractIntegrationTests.HandlerCamelCaseNameFromClasses.class)
public abstract class AbstractIntegrationTests {

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractIntegrationTests.class);
    private static final GenericContainer<?> POSTGRES = new GenericContainer<>(DockerImageName.parse("postgres:13.2-alpine"))
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
            .withEnv("POSTGRES_USER", "central")
            .withEnv("POSTGRES_PASSWORD", "central")
            .withEnv("POSTGRES_DB", "central");

    static {
        POSTGRES.setPortBindings(Collections.singletonList(String.format("%d:%d/%s", 5434, 5432, InternetProtocol.TCP.toDockerNotation())));
        POSTGRES.start();
        migrateCentralTenant();
    }

    @Autowired
    protected MockMvc mockMvc;

    /**
     * Migrate our tenant (central tenant). Database central and schema public
     */
    protected static void migrateCentralTenant() {
        try {
            final Flyway flyway = new FluentConfiguration().dataSource(CENTRAL_TENANT.getDataSource())
                    .schemas(CENTRAL_TENANT.getSchema()).baselineOnMigrate(true).locations("db/migration").load();
            flyway.migrate();
        } catch (final Exception e) {
            LOGGER.error("Error while migrating tenant {}", CENTRAL_TENANT.getSchema(), e);
            throw new RuntimeException(e);
        }
    }

    public static class HandlerCamelCaseNameFromClasses extends DisplayNameGenerator.Standard {

        @Override
        public String generateDisplayNameForClass(Class<?> classeDeTeste) {
            return substituirCamelCase(super.generateDisplayNameForClass(classeDeTeste));
        }

        @Override
        public String generateDisplayNameForNestedClass(Class<?> classesFilhas) {
            return substituirCamelCase(super.generateDisplayNameForNestedClass(classesFilhas));
        }

        @Override
        public String generateDisplayNameForMethod(Class<?> testeDeClasse, Method testeDoMetodo) {
            return substituirCamelCase(testeDoMetodo.getName()) +
                    DisplayNameGenerator.parameterTypesAsString(testeDoMetodo);
        }

        private static String substituirCamelCase(String classeOuMetodoEmCamelCase) {
            StringBuilder resultado = new StringBuilder();
            resultado.append(classeOuMetodoEmCamelCase.charAt(0));
            for (int i = 1; i < classeOuMetodoEmCamelCase.length(); i++) {
                if (Character.isUpperCase(classeOuMetodoEmCamelCase.charAt(i))) {
                    resultado.append(' ');
                    resultado.append(Character.toLowerCase(classeOuMetodoEmCamelCase.charAt(i)));
                } else {
                    resultado.append(classeOuMetodoEmCamelCase.charAt(i));
                }
            }
            return resultado.toString();
        }
    }

}