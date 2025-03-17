package com.emanuelvictor.erp.infrastructure.multitenant;

import lombok.Setter;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.emanuelvictor.erp.infrastructure.multitenant.TenantDAO.CENTRAL_TENANT;

@Setter
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    private String tenant;

    @Override
    public String resolveCurrentTenantIdentifier() {
        return tenant == null ? CENTRAL_TENANT.getSchema() : tenant;
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
    public boolean isRoot(String tenant) {
        assert tenant != null;
        return tenant.equals(CENTRAL_TENANT.getSchema());
    }
}
