package com.emanuelvictor.erp.infrastructure.multitenant.schema;

import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDetails;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO.CENTRAL_TENANT;
import static com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO.getAllCostumerTenants;


@Component
@RequiredArgsConstructor
public class ConnectionProvider implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {

    @Override
    public Connection getAnyConnection() throws SQLException {
        return CENTRAL_TENANT.getDataSource().getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String schema) throws SQLException {
        if (CENTRAL_TENANT.getSchema().equals(schema)) {
            final Connection connection = CENTRAL_TENANT.getDataSource().getConnection();
            connection.setSchema(schema);
            return connection;
        }

        final TenantDetails tenantDetails = Optional.ofNullable(getAllCostumerTenants().get(schema))
                .orElseThrow(() -> new RuntimeException("Tenant not found. Verify if it is added in RouteDataSource of TenantMigrationService"));
        final Connection connection = tenantDetails.getDataSource().getConnection();
        connection.setSchema(schema);
        return connection;
    }

    @Override
    public void releaseConnection(String schema, Connection connection) throws SQLException {
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