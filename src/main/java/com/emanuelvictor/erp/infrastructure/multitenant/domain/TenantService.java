package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TenantService {

    public List<TenantTable> getCostumerTenants() { // TODO make this search by native
//        Tenant client1 = new Tenant("client1", "central", "127.0.0.1");
//        Tenant client2 = new Tenant("client2", "central", "127.0.0.1");
//        Tenant client3 = new Tenant("client3", "client3", "127.0.0.1");
//        Tenant client4 = new Tenant("client4", "central", "127.0.0.1");
//        Tenant client5 = new Tenant("client5", "central", "127.0.0.1");
//        return Arrays.asList(client1, client2, client3, client4, client5);
        return Collections.emptyList();
    }

    public static TenantTable getCentralTenant() {
        return new TenantTable("public", "central", "127.0.0.1");
    }

}
