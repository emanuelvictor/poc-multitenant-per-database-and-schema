//package com.emanuelvictor;
//
//import com.emanuelvictor.domain.Tenant;
//import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
//import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
//import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//public class MapMultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider {
//
//    private final Map<String, ConnectionProvider> connectionProviderMap = new HashMap<>();
//
//    public MapMultiTenantConnectionProvider() throws IOException, SQLException {
//        initConnectionProviderForTenant("tenant1");
//        initConnectionProviderForTenant("tenant2");
//    }
//
//    @Override
//    protected ConnectionProvider getAnyConnectionProvider() {
//        return connectionProviderMap.values()
//                .iterator()
//                .next();
//    }
//
//    @Override
//    protected ConnectionProvider selectConnectionProvider(Object tenantIdentifier) {
//        return connectionProviderMap.get(tenantIdentifier);
//    }
//
//    private void initConnectionProviderForTenant(Tenant tenant) throws IOException, SQLException {
//        Properties properties = new Properties();
//        properties.load(getClass().getResourceAsStream(String.format("/tenant-database-default-configuration.properties", tenant.tenantId())));
//        properties.put("propriedade", tenant.getPropriedade());
//        Map<String, Object> configProperties = new HashMap<>();
//        for (String key : properties.stringPropertyNames()) {
//            String value = properties.getProperty(key);
//            configProperties.put(key, value);
//        }
//        DriverManagerConnectionProviderImpl connectionProvider = new DriverManagerConnectionProviderImpl();
//        connectionProvider.configure(configProperties);
//        connectionProvider.getConnection().setSchema(tenant.schema());
//        this.connectionProviderMap.put(tenant.tenantId(), connectionProvider);
//    }
//
//}