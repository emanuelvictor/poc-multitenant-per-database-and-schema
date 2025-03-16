package com.emanuelvictor.erp.infrastructure.multitenant;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService.CENTRAL_DATA_SOURCE;
import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService.getAllCostumerTenants;

@Component
@RequiredArgsConstructor
public class ConnectionProvider implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {

//    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException { // TODO sei lá
        return CENTRAL_DATA_SOURCE.getDataSource().getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String schema) throws SQLException {
        if (CENTRAL_DATA_SOURCE.getSchema().equals(schema))
            return CENTRAL_DATA_SOURCE.getDataSource().getConnection();

        final Connection connection = getAllCostumerTenants().stream()
                .filter(tenant -> tenant.getSchema().equals(schema.toLowerCase()))
                .findFirst().orElseThrow(() -> new RuntimeException("Tenant not found. Verify if it is added in RouteDataSource of TenantMigrationService"))
                .getDataSource().getConnection();
        connection.setSchema(schema);
        return connection;
    }

    @Override
    public void releaseConnection(String schema, Connection connection) throws SQLException {
//        connection.setSchema("public"); // TODO ??
        connection.setSchema(schema);
        connection.close();
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }


// empty overrides skipped for brevity


    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }


    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}