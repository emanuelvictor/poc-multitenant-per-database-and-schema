package com.emanuelvictor.erp.domain.tenants;

import com.emanuelvictor.erp.AbstractIntegrationTests;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(AbstractIntegrationTests.HandlerCamelCaseNameFromClasses.class)
public class TenantTest {

    @Test
    void testCreateValidTenant() {
        Tenant tenant = Tenant.create("tenant1", "tenant1", "127.0.0.1");
        assertNotNull(tenant);
        assertEquals("tenant1", tenant.getSchema());
        assertEquals("tenant1", tenant.getDatabase());
        assertEquals("127.0.0.1", tenant.getAddress());
    }

    @Test
    void testCreateTenantWithNullValues() {
        Exception exception = assertThrows(RuntimeException.class, () ->
                Tenant.create(null, "database", "127.0.0.1"));
        assertEquals("The tenant schema, database and address are required", exception.getMessage());
    }

    @Test
    void testCreateTenantWithBlankValues() {
        Exception exception = assertThrows(RuntimeException.class, () ->
                Tenant.create("", "database", "127.0.0.1"));
        assertEquals("The tenant schema, database and address are required", exception.getMessage());
    }

    @Test
    void testCreateTenantWithMismatchedSchemaAndDatabase() {
        Exception exception = assertThrows(RuntimeException.class, () ->
                Tenant.create("tenant1", "db1", "127.0.0.1"));
        assertEquals("In tenants with their own database the schema and database must be equals", exception.getMessage());
    }

    @Test
    void testCreateTenantWithCentralDatabase() {
        Tenant tenant = Tenant.create("central", "central", "127.0.0.1");
        assertNotNull(tenant);
        assertEquals("central", tenant.getSchema());
        assertEquals("central", tenant.getDatabase());
    }
}