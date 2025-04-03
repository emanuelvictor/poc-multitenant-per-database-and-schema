package com.emanuelvictor.erp.application.services.tenants.getalltenants;

import com.emanuelvictor.erp.AbstractIntegrationTests;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static com.emanuelvictor.erp.infrastructure.multitenant.TenantDAO.dropAllTenants;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetAllTenantsRestTest extends AbstractIntegrationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        migrateCentralTenant();
    }

    @Test
    void mustGetAllTenants() throws Exception {
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
        final List<TenantDTO> tenantsExpected =
                asList(tenant1, tenant2, tenant3, tenant4, tenant5, tenant6, tenant7, tenant8);
        tenantsExpected.forEach(this::addTenant);

        var result = mockMvc.perform(get("/tenants"))
                .andExpect(status().isOk())
                .andReturn();
        final List<TenantDTO> tenantsObtained = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(tenantsObtained).usingRecursiveAssertion().isEqualTo(tenantsExpected);
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

    @AfterEach
    void tearDown() {
        dropAllTenants();
    }
}
