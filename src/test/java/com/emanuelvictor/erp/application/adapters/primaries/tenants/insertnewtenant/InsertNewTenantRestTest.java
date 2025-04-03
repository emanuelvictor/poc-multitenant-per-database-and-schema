package com.emanuelvictor.erp.application.adapters.primaries.tenants.insertnewtenant;

import com.emanuelvictor.erp.AbstractIntegrationTests;
import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO;
import com.emanuelvictor.erp.infrastructure.multitenant.database.RoutingDataSourceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Stream;

import static com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO.dropAllTenants;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Casos de teste:
 * 1 - Criar tenant client1 no banco central, inserir e resgatar um produto; - OK
 * 2 - Criar tenant client2 no banco central, inserir e resgatar um produto; - OK
 * 3 - Criar tenant client3 com banco dedicado, inserir e resgatar um produto; - OK
 * 4 - Criar tenant client4 no banco central, inserir e resgatar um produto; - OK
 * 5 - Criar tenant client5 no banco central, inserir e resgatar um produto; - OK
 * 6 - Criar tenant client6 com banco dedicado, inserir e resgatar um produto; - OK
 * 7 - Criar tenant client7 com banco dedicado, inserir e resgatar um produto; - OK
 * 8 - Criar tenant client8 com banco dedicado, inserir e resgatar um produto; - OK
 * 9 - Deve haver 5 bancos de dados (exceto a base "postgres"); - OK
 * 10 - A quantidade de conexões abertas deve estar em torno de 57 (7 conexões do próprio postgres, e 10 para cada banco de dados) - OK
 * 11 - No banco central deve haver 5 esquemas (excluindo o "public") - OK
 * 12 - Todos os tenants devem estar migrados - OK
 * 13 - Reiniciar aplicação e verificar se não dará nenhum erro - OK
 * 14 - Ao reiniciar a aplicação as conexões deverão voltar a 57 - OK
 * <p>
 */
public class InsertNewTenantRestTest extends AbstractIntegrationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoutingDataSourceService routingDataSourceService;

    @BeforeEach
    void setUp() {
        migrateCentralTenant();
        routingDataSourceService.configureRoutingDataSources();
    }

    @ParameterizedTest
    @MethodSource("getDataToAddTenants")
    void mustAddTenantsAndVerifyResolvedDataSourcesAndDataSources(final List<TenantDTO> tenants,
                                                                  final int countResolvedDataSourcesExpected,
                                                                  final int countOfDataSourcesExpected,
                                                                  final int countOfDatabasesCreatedExpected) {

        tenants.forEach(tenantDTO -> {
            addTenant(tenantDTO);
            addProductInTenant(tenantDTO);
            verifyProductsFromTenant(tenantDTO);
        });

        verifyCountOfConnectionsToDatabase(countOfDataSourcesExpected);
        verifyCountOfResolvedDataSources(countResolvedDataSourcesExpected);
        verifyCountOfDataSources(countOfDataSourcesExpected);
        verifyCountOfDatabases(countOfDatabasesCreatedExpected);
    }

    private static Stream<Arguments> getDataToAddTenants() {
        final String centralDatabase = "central";
        final String address = "127.0.0.1";
        final TenantDTO tenant1 = new TenantDTO("client1", centralDatabase, address);
        final TenantDTO tenant2 = new TenantDTO("client2", centralDatabase, address);
        final TenantDTO tenant3 = new TenantDTO("client3", "client3", address);
        final TenantDTO tenant4 = new TenantDTO("client4", centralDatabase, address);
        final TenantDTO tenant5 = new TenantDTO("client5", centralDatabase, address);
        final TenantDTO tenant6 = new TenantDTO("client6", "client6", address);
        final TenantDTO tenant7 = new TenantDTO("client7", "client7", address);
        final TenantDTO tenant8 = new TenantDTO("client8", "client8", address);
        return Stream.of(
                arguments(Collections.emptyList(), 0, 0, 1),
                arguments(Collections.singletonList(tenant1), 1, 1, 1),
                arguments(Arrays.asList(tenant1, tenant2), 2, 1, 1),
                arguments(Arrays.asList(tenant1, tenant2, tenant3), 3, 2, 2),
                arguments(Arrays.asList(tenant1, tenant2, tenant3, tenant4), 4, 2, 2),
                arguments(Arrays.asList(tenant1, tenant2, tenant3, tenant4, tenant5), 5, 2, 2),
                arguments(Arrays.asList(tenant1, tenant2, tenant3, tenant4, tenant5, tenant6), 6, 3, 3),
                arguments(Arrays.asList(tenant1, tenant2, tenant3, tenant4, tenant5, tenant6, tenant7), 7, 4, 4),
                arguments(Arrays.asList(tenant1, tenant2, tenant3, tenant4, tenant5, tenant6, tenant7, tenant8), 8, 5, 5),
                Arguments.arguments(generateSchemaTenants(50), 50, 1, 1),
//                Arguments.arguments(generateSchemaTenants(2000), 2000, 1, 1),
                arguments(generateDatabaseTenants(5), 5, 5, 6),
                arguments(generateDatabaseTenants(9), 9, 9, 10)
        );
    }

    private static List<TenantDTO> generateSchemaTenants(int countOfTenants) {
        final List<TenantDTO> tenants = new ArrayList<>();
        for (int i = 1; i <= countOfTenants; i++) {
            final TenantDTO tenantDTO = new TenantDTO("client" + i, "central", "127.0.0.1");
            tenants.add(tenantDTO);
        }
        return tenants;
    }

    private static List<TenantDTO> generateDatabaseTenants(int countOfTenants) {
        if (countOfTenants > 9) {
            // The max of databases is 10, because each datasource must have 10 connections from pool of connections.
            // Postgres has a limit of 100 connections.
            throw new IllegalArgumentException("Too many databases");
        }
        final List<TenantDTO> tenants = new ArrayList<>();
        for (int i = 1; i <= countOfTenants; i++) {
            final TenantDTO tenantDTO = new TenantDTO("client" + i, "client" + i, "127.0.0.1");
            tenants.add(tenantDTO);
        }
        return tenants;
    }

    private void verifyProductsFromTenant(TenantDTO tenantDTO) {
        final var productDTO = new ProductDTO("Bola do cliente " + tenantDTO.schema());
        try {
            var result = mockMvc.perform(get("/products")
                            .header("schema", tenantDTO.schema()))
                    .andExpect(status().isOk())
                    .andReturn();
            final List<ProductDTO> listDeProdutos = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertThat(listDeProdutos.size()).isEqualTo(1);
            assertThat(listDeProdutos.get(0).name()).isEqualTo(productDTO.name());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void addProductInTenant(TenantDTO tenantDTO) {
        final var productDTO = new ProductDTO("Bola do cliente " + tenantDTO.schema());
        try {
            final String json = objectMapper.writeValueAsString(productDTO);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("schema", tenantDTO.schema())
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value(productDTO.name()));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void addTenant(TenantDTO tenantDTO) {
        try {
            final String json = objectMapper.writeValueAsString(tenantDTO);

            mockMvc.perform(post("/tenants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
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

    private void verifyCountOfDataSources(final int countOfDataSourcesExpected) {
        final Set<DataSource> dataSources = new HashSet<>();
        routingDataSourceService.getResolvedDataSources().forEach((ignore, dataSource) -> {
            dataSources.add(dataSource);
        });
        assertThat(dataSources.size()).isEqualTo(countOfDataSourcesExpected);
    }

    /**
     * Must have 10 connections per database
     *
     * @param countOfDataSourcesExpected int
     */
    private static void verifyCountOfConnectionsToDatabase(final int countOfDataSourcesExpected) {
        assertThat(TenantDAO.getTotalOfConnections() >= countOfDataSourcesExpected).isTrue();
    }

    private static void verifyCountOfDatabases(final int countOfDatabasesExpected) {
        assertThat(TenantDAO.getTotalOfDatabases()).isEqualTo(countOfDatabasesExpected);
    }

    @AfterEach
    void tearDown() {
        dropAllTenants();
    }
}
