package com.emanuelvictor.erp.application.web;

import com.emanuelvictor.erp.application.filters.TenantFilter;
import com.emanuelvictor.erp.infrastructure.multitenant.TenantIdentifierResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans {

    @Bean
    public FilterRegistrationBean<?> tenantFilter(final TenantIdentifierResolver tenantIdentifierResolver) {
        final FilterRegistrationBean<TenantFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantFilter(tenantIdentifierResolver));
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}