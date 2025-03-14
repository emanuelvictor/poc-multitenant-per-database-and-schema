package com.emanuelvictor.erp.infrastructure.multitenant;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantTable;
import lombok.Setter;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantService.getCentralTenant;

@Setter
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<TenantTable>, HibernatePropertiesCustomizer {

    private TenantTable currentTenantTable;

    @Override
    public TenantTable resolveCurrentTenantIdentifier() {
        return currentTenantTable == null ? getCentralTenant() : currentTenantTable;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

    // empty overrides skipped for brevity


    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public boolean isRoot(TenantTable tenantTable) {
        assert tenantTable != null;
        return tenantTable.isCentral();
    }
}
