package com.emanuelvictor.erp.infrastructure.multitenant;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantDetails;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantService;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantTableRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;

import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantService.getCentralTenant;
import static java.util.Optional.ofNullable;

@Component
public class TenantRoutingDatasource extends AbstractRoutingDataSource {

    private final TenantIdentifierResolver tenantIdentifierResolver;

    TenantRoutingDatasource(final TenantService tenantService, TenantIdentifierResolver tenantIdentifierResolver) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;

        setDefaultTargetDataSource(dataSourceFromTenant(getCentralTenant()));

        final HashMap<Object, Object> targetDataSources = new HashMap<>();
        tenantService.getCostumerTenants().forEach(tenant -> targetDataSources.put(tenant.getSchema(), dataSourceFromTenant(tenant)));
        setTargetDataSources(targetDataSources);
    }

    @Override
    protected String determineCurrentLookupKey() {
        return ofNullable(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).orElse(getCentralTenant()).getDatabase();
    }

    public static DataSource dataSourceFromTenant(TenantDetails tenantDetails) {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver"); // TODO put in env
        dataSource.setUsername("central");  // TODO put in env
        dataSource.setPassword("central");  // TODO put in env
        dataSource.setPoolName(tenantDetails.getSchema());
        dataSource.setMaximumPoolSize(10); // TODO put in tenant
        dataSource.setJdbcUrl("jdbc:postgresql://" + tenantDetails.getAddress() + ":5432/" + tenantDetails.getDatabase()); // TODO put in tenant

        return dataSource;
    }
}
