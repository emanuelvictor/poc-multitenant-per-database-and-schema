package com.emanuelvictor.erp.infrastructure.multitenant;

import com.emanuelvictor.erp.AbstractIntegrationTests;
import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TTenant;
import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO;
import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TenantDAOTest extends AbstractIntegrationTests {

    @BeforeEach
    public void setup() {
        migrateCentralTenant();
    }

    @Test
    void mustAddNewTenant() {
        TenantDetails tenant = new TTenant("tenant1", "db1", "127.0.0.1", null);
        addNewTenant(tenant);
        assertEquals(1, TenantDAO.getAllCostumerTenants().size());
    }

    @Test
    void mustGetTotalOfDatabases() {
        int totalDatabases = TenantDAO.getTotalOfDatabases();
        assertEquals(1, totalDatabases); // Pelo menos a "central" sempre existirá
    }

    @Test
    void mustGetTotalOfConnections() {
        int totalConnections = TenantDAO.getTotalOfConnections();
        assertTrue(totalConnections > 0);
    }

    @Test
    void mustDropAllTenants() {
        TenantDetails tenant = new TTenant("tenant2", "db2", "127.0.0.1", null);
        addNewTenant(tenant);
        createNewDatabase(tenant.getDatabase());
        assertFalse(TenantDAO.getAllCostumerTenants().isEmpty());
        dropAllTenants();
        assertThat(TenantDAO.getTotalOfDatabases()).isEqualTo(1);  // Pelo menos a "central" sempre existirá
    }

    @AfterEach
    public void afterEach() {
        dropAllTenants();
    }
}
