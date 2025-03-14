package com.emanuelvictor.erp.application.services.getAllTenants;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetAllTenantsRest {

    private final TenantTableRepository tenantTableRepository;

    @GetMapping("tenants")
    public List<TenantDTO> getAllTenants() { // TODO testint
        return tenantTableRepository.findAll().stream()
                .map(tenantTable -> new TenantDTO(tenantTable.getSchema(), tenantTable.getDatabase(),
                        tenantTable.getAddress())).toList();
    }
}
