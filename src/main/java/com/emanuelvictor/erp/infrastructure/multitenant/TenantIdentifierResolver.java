package com.emanuelvictor.erp.infrastructure.multitenant;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.Tenant;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<Tenant>, HibernatePropertiesCustomizer {

    private Tenant currentTenant;

    public void setCurrentTenant(Tenant tenant) {
        currentTenant = tenant;
    }

    @Override
    public Tenant resolveCurrentTenantIdentifier() {
        return currentTenant;
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
    public boolean isRoot(Tenant tenant) {
        assert tenant != null;
        return tenant.isCentral();
    }
}
