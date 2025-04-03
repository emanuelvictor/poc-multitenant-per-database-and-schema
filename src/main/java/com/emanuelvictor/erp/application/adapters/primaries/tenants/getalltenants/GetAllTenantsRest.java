package com.emanuelvictor.erp.application.adapters.primaries.tenants.getalltenants;

import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetAllTenantsRest {

    @GetMapping("tenants")
    public List<TenantDTO> getAllTenants() {
        final List<TenantDTO> tenantDTOs = new ArrayList<>();
        TenantDAO.getAllCostumerTenants().forEach((schema, tenantDetails) ->
                tenantDTOs.add(new TenantDTO(schema, tenantDetails.getDatabase(), tenantDetails.getAddress())));
        return tenantDTOs;
    }
}
