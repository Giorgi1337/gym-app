package com.gym.config;

import com.gym.filter.RequestResponseLoggingFilter;
import com.gym.filter.TransactionIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> requestResponseLoggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> reg = new FilterRegistrationBean<>(new RequestResponseLoggingFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(2);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<TransactionIdFilter> transactionIdFilter() {
        FilterRegistrationBean<TransactionIdFilter> reg = new FilterRegistrationBean<>(new TransactionIdFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }

}