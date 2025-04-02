package com.emanuelvictor.erp.application.services.tenants.insertnewtenant;

import com.emanuelvictor.erp.HandlerCamelCaseNameFromClasses;
import com.emanuelvictor.erp.IntegrationTests;
import com.emanuelvictor.erp.infrastructure.multitenant.RoutingDataSourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Stream;

import static com.emanuelvictor.erp.infrastructure.multitenant.TenantDAO.CENTRAL_TENANT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


/**
 * Casos de teste:
 * 1 - Criar tenant client1 no banco central, inserir e resgatar um produto;
 * 2 - Criar tenant client2 no banco central, inserir e resgatar um produto;
 * 3 - Criar tenant client3 com banco dedicado, inserir e resgatar um produto;
 * 4 - Criar tenant client4 no banco central, inserir e resgatar um produto;
 * 5 - Criar tenant client5 no banco central, inserir e resgatar um produto;
 * 6 - Criar tenant client6 com banco dedicado, inserir e resgatar um produto;
 * 7 - Criar tenant client7 com banco dedicado, inserir e resgatar um produto;
 * 8 - Criar tenant client8 com banco dedicado, inserir e resgatar um produto;
 * 9 - Deve haver 5 bancos de dados (exceto a base "postgres");
 * 10 - A quantidade de conexões abertas deve estar em torno de 57 (7 conexões do próprio postgres, e 10 para cada banco de dados) - OK
 * 11 - No banco central deve haver 5 esquemas (excluindo o "public") - OK
 * 12 - Todos os tenants devem estar migrados - OK
 * 13 - Reiniciar aplicação e verificar se não dará nenhum erro - ERRO, ao reiniciar está migrando todos os tenants para o central
 * 14 - Ao reiniicar a aplicação as conexões deverão voltar a 57 - ERRO não está voltando as 57 conexões.
 * <p>
 * <p>
 * bug quando seta um tenant e volta a inserir um tenant - RESOLVIDO
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(HandlerCamelCaseNameFromClasses.class)
public class RestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTest.class);
    private static final GenericContainer<?> postgres = new GenericContainer<>(DockerImageName.parse("postgres:13.2-alpine"))
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
            .withEnv("POSTGRES_USER", "central")
            .withEnv("POSTGRES_PASSWORD", "central")
            .withEnv("POSTGRES_DB", "central");

    static {
        postgres.setPortBindings(Collections.singletonList(String.format("%d:%d/%s", 5434, 5432, InternetProtocol.TCP.toDockerNotation())));
        postgres.start();
        migrateCentralTenant();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoutingDataSourceService routingDataSourceService;

    @BeforeEach
    void setUp() {
        postgres.start();
        migrateCentralTenant();
    }

    @Test
    void mustHaveZeroDatasourceWithoutTenants() {
        assertThat(routingDataSourceService.getResolvedDataSources().size()).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("getDataToAddTenants")
    void mustAddTenantsAndVerifyResolvedDataSourcesAndDataSources(List<TenantDTO> tenants, final int countResolvedDataSources, final int countOfDataSources) {

        tenants.forEach(this::addTenant);

        verifyCountOfResolvedDataSources(countResolvedDataSources);
        verifyCountOfDataSources(countOfDataSources);
    }

    private static Stream<Arguments> getDataToAddTenants() {
        final String centralDatabase = "central";
        final String address = "127.0.0.1";
        final TenantDTO tenant1 = new TenantDTO("client1", centralDatabase, address);
        final TenantDTO tenant2 = new TenantDTO("client2", centralDatabase, address);
        return Stream.of(
                Arguments.arguments(Collections.emptyList(), 0, 0),
                Arguments.arguments(Collections.singletonList(tenant1), 1, 1),
                Arguments.arguments(Arrays.asList(tenant1, tenant2), 2, 1)
        );
    }

    private void addTenant(TenantDTO tenantDTO) {
        try {
            final String json = objectMapper.writeValueAsString(tenantDTO);

            mockMvc.perform(MockMvcRequestBuilders.post("/tenants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(jsonPath("$.schema").value(tenantDTO.schema()))
                    .andExpect(jsonPath("$.database").value(tenantDTO.database()))
                    .andExpect(jsonPath("$.address").value(tenantDTO.address()));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyCountOfResolvedDataSources(final int countResolvedDataSources) {
        assertThat(routingDataSourceService.getResolvedDataSources().size()).isEqualTo(countResolvedDataSources);
    }

    private void verifyCountOfDataSources(final int countOfDataSources) {
        final Set<DataSource> dataSources = new HashSet<>();
        routingDataSourceService.getResolvedDataSources().forEach((ignore, dataSource) -> {
            dataSources.add(dataSource);
        });
        assertThat(dataSources.size()).isEqualTo(countOfDataSources);
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

    @AfterEach
    void tearDown() {
        postgres.stop();
    }
}
