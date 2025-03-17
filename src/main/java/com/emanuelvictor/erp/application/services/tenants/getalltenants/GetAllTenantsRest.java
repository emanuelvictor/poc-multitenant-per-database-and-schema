package com.emanuelvictor.erp.application.services.tenants.getalltenants;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.TTenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetAllTenantsRest {

    private final TTenantRepository tTenantRepository;

    @GetMapping("tenants")
    public List<TenantDTO> getAllTenants() { // TODO test it
        return tTenantRepository.findAll().stream()
                .map(tTenant -> new TenantDTO(tTenant.getSchema(), tTenant.getDatabase(),
                        tTenant.getAddress())).toList();
    }
}
