package com.emanuelvictor.erp.infrastructure.multitenant.database;

import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDetails;
import com.emanuelvictor.erp.infrastructure.multitenant.schema.TenantIdentifierResolver;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import static com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO.CENTRAL_TENANT;
import static com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO.getAllCostumerTenants;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class RoutingDataSourceService extends AbstractRoutingDataSource {

    private final TenantIdentifierResolver tenantIdentifierResolver;

    @PostConstruct
    public void postConstruct() {
        configureRoutingDataSources();
    }

    public void configureRoutingDataSources() {
        setDefaultTargetDataSource(CENTRAL_TENANT.getDataSource());

        final HashMap<String, TenantDetails> costumerTenants = getAllCostumerTenants();
        final HashMap<Object, Object> targetDataSources = new HashMap<>();
        costumerTenants.forEach((schema, tenantDetails) -> targetDataSources.put(schema, tenantDetails.getDataSource()));
        setTargetDataSources(targetDataSources);

        initialize();
    }

    @Override
    protected String determineCurrentLookupKey() {
        final TenantDetails tenantDetails =
                ofNullable(getAllCostumerTenants().get(tenantIdentifierResolver.resolveCurrentTenantIdentifier()))
                        .orElse(CENTRAL_TENANT);
        return tenantDetails.getDatabase();
    }
}
