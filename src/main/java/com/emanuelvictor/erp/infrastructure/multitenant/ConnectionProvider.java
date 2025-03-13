package com.emanuelvictor.erp.infrastructure.multitenant;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.Tenant;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantsService;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component
public class ConnectionProvider implements MultiTenantConnectionProvider<Tenant>, HibernatePropertiesCustomizer {

    @Autowired
    DataSource dataSource;

    @Autowired
    TenantsService tenantsService;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return getConnection(tenantsService.getDefaultTenant());
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Tenant tenant) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setSchema(tenant.schema());
        return connection;
    }

    @Override
    public void releaseConnection(Tenant tenant, Connection connection) throws SQLException {
//        connection.setSchema("public"); // TODO ??
        connection.setSchema(tenant.schema());
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