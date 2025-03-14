package com.emanuelvictor.erp.infrastructure.multitenant;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantTable;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantService.getCentralTenant;

@Component
@RequiredArgsConstructor
public class ConnectionProvider implements MultiTenantConnectionProvider<TenantTable>, HibernatePropertiesCustomizer {

    private final DataSource dataSource;

//    @Autowired
//    TenantsService tenantsService;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return getConnection(getCentralTenant());
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(TenantTable tenantTable) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setSchema(tenantTable.getSchema());
        return connection;
    }

    @Override
    public void releaseConnection(TenantTable tenantTable, Connection connection) throws SQLException {
//        connection.setSchema("public"); // TODO ??
        connection.setSchema(tenantTable.getSchema());
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