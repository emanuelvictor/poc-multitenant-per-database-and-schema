//package com.emanuelvictor;
//
//import com.emanuelvictor.domain.Tenant;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
//import org.springframework.jdbc.datasource.embedded.DataSourceFactory;
//
//import javax.sql.DataSource;
//
//public class MyDataSourceFactory implements DataSourceFactory {
//
//    private final Tenant tenant;
//
//    public MyDataSourceFactory(Tenant tenant) {
//        this.tenant = tenant;
//    }
//
//    @Override
//    public ConnectionProperties getConnectionProperties() {
//        ConnectionProperties connectionProperties = new ConnectionPropertiesImpl();
//        connectionProperties.setUsername("admin@admin.com");  // TODO put in env
//        connectionProperties.setPassword("admin@123"); // TODO put in env
//        connectionProperties.setUrl("jdbc:postgresql://" + tenant.ipAddress() + ":5432/" + tenant.database()); // TODO put in tenant
//        return connectionProperties;
//    }
//
//    @Override
//    public DataSource getDataSource() {
//        return dataSourceFromTenant(tenant);
//    }
//
//    public DataSource dataSourceFromTenant(Tenant tenant) {
//        final HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver"); // TODO put in env
//        dataSource.setMaximumPoolSize(100); // TODO put in tenant
//        dataSource.setPoolName(tenant.tenantId());
//        dataSource.setJdbcUrl("jdbc:postgresql://" + tenant.ipAddress() + ":5432/" + tenant.database()); // TODO put in tenant
//        dataSource.setUsername("admin@admin.com");  // TODO put in env
//        dataSource.setPassword("admin@123");  // TODO put in env
//        return dataSource;
//    }
//}
