package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService.CENTRAL_DATA_SOURCE;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "tenant")
public class TTenant implements TenantDetails {

    @Id
    private String schema;
    @Column(nullable = false)
    private String database;
    @Column(nullable = false)
    private String address;
    @Transient
    private DataSource dataSource;

    public TTenant(String schema, String database, String address, DataSource dataSource) {
        this.schema = schema;
        this.database = database;
        this.address = address;
        this.dataSource = dataSource;
    }

    public boolean isCentral() {
        return schema.equals(CENTRAL_DATA_SOURCE.getSchema());
    }

    @Override
    public DataSource getDataSource() {
        if(dataSource == null) {
            dataSource = dataSourceFromTenant(this);
        }
        return dataSource;
    }

    private static DataSource dataSourceFromTenant(TenantDetails tenantDetails) {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver"); // TODO put in env
        dataSource.setUsername("central");  // TODO put in env
        dataSource.setPassword("central");  // TODO put in env
        dataSource.setPoolName(tenantDetails.getSchema());
        dataSource.setMaximumPoolSize(10); // TODO put in tenant
        dataSource.setJdbcUrl("jdbc:postgresql://" + tenantDetails.getAddress() + ":5433/" + tenantDetails.getDatabase()); // TODO put in tenant

        return dataSource;
    }


}
