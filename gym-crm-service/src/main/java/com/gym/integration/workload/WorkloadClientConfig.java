package com.gym.integration.workload;

import com.gym.filter.TransactionIdFilter;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkloadClientConfig {

    @Bean
    RequestInterceptor workloadRequestInterceptor(ServiceJwtProvider jwtProvider) {
        return template -> {
            template.header("Authorization", "Bearer " + jwtProvider.token());
            String transactionId = MDC.get(TransactionIdFilter.MDC_KEY);
            if (transactionId != null) {
                template.header(TransactionIdFilter.HEADER, transactionId);
            }
        };
    }
}
