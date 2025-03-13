package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TenantsService {

    public List<Tenant> getAllTenants() {
        Tenant client1 = new Tenant("default", "client1", "127.0.0.1");
        Tenant client2 = new Tenant("default", "client2", "127.0.0.1");
        Tenant client3 = new Tenant("client3", "client3", "127.0.0.1");
        Tenant client4 = new Tenant("default", "client4", "127.0.0.1");
        Tenant client5 = new Tenant("default", "client5", "127.0.0.1");
        return Arrays.asList(client1, client2, client3, client4, client5);
    }

    public Tenant getDefaultTenant() {
        return new Tenant("central", "public", "127.0.0.1");
    }

}
