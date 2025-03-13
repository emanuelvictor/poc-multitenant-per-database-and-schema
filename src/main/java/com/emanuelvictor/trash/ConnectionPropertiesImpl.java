//package com.emanuelvictor;
//
//import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
//
//import java.sql.Driver;
//
//public class ConnectionPropertiesImpl implements ConnectionProperties {
//
//    private String url;
//    private String username;
//    private String password;
//    private Class<? extends Driver> driverClass;
//
//    @Override
//    public void setDriverClass(Class<? extends Driver> driverClass) {
//        this.driverClass = driverClass;
//    }
//
//    @Override
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    @Override
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    @Override
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public Class<? extends Driver> getDriverClass() {
//        return driverClass;
//    }
//}
