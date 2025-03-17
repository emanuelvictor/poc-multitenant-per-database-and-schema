package com.emanuelvictor.erp.domain.tenants;

import lombok.Getter;

@Getter
public class Tenant {

    private final String schema;
    private final String database;
    private final String address;

    private Tenant(String schema, String database, String address) {
        validateData(schema, database, address);
        this.schema = schema;
        this.database = database;
        this.address = address;
    }

    public static Tenant create(String schema, String database, String address) {
        return new Tenant(schema, database, address);
    }

    private static void validateData(String schema, String database, String address) {
        if (schema == null || database == null || address == null)
            throw new RuntimeException("The tenant schema, database and address are required");
        if (schema.isBlank() || database.isBlank() || address.isBlank())
            throw new RuntimeException("The tenant schema, database and address are required");
        validateIfTheTenantCreateToCustomDatabaseHasTheSchemaAndDatabaseEquals(schema, database);
    }

    private static void validateIfTheTenantCreateToCustomDatabaseHasTheSchemaAndDatabaseEquals(String schema, String database) {
        if (!database.equals("central") && !schema.equals(database))
            throw new RuntimeException("In tenants with their own database the schema and database must be equals");
    }

}
