package com.emanuelvictor.erp.infrastructure.multitenant;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.Tenant;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantsService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;

import static java.util.Optional.ofNullable;

@Component
public class TenantRoutingDatasource extends AbstractRoutingDataSource {

    private final TenantsService tenantsService;

    @Autowired
    private TenantIdentifierResolver tenantIdentifierResolver;

    TenantRoutingDatasource(final TenantsService tenantsService) {
        this.tenantsService = tenantsService;

        setDefaultTargetDataSource(dataSourceFromTenant(tenantsService.getDefaultTenant()));

        final HashMap<Object, Object> targetDataSources = new HashMap<>();
        tenantsService.getAllTenants().forEach(tenant -> targetDataSources.put(tenant.schema(), dataSourceFromTenant(tenant)));
        setTargetDataSources(targetDataSources);
    }

    @Override
    protected String determineCurrentLookupKey() {
        return ofNullable(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).orElse(tenantsService.getDefaultTenant()).database();
    }

//    private EmbeddedDatabase createEmbeddedDatabase(Tenant tenant) {
//        return new EmbeddedDatabaseBuilder().setDataSourceFactory(new MyDataSourceFactory(tenant)).build();
//    }

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
//    }

    public DataSource dataSourceFromTenant(Tenant tenant) {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver"); // TODO put in env
        dataSource.setUsername("central");  // TODO put in env
        dataSource.setPassword("central");  // TODO put in env
        dataSource.setPoolName(tenant.schema());
        dataSource.setMaximumPoolSize(10); // TODO put in tenant
        dataSource.setJdbcUrl("jdbc:postgresql://" + tenant.ipAddress() + ":5432/" + tenant.database()); // TODO put in tenant

        return dataSource;
    }
}
