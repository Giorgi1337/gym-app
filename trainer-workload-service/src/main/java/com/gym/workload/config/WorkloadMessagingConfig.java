package com.gym.workload.config;

import com.gym.workload.dto.TrainerWorkloadRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

@Configuration
public class WorkloadMessagingConfig {

    @Bean
    MessageConverter workloadMessageConverter() {
        var converter = new JacksonJsonMessageConverter();
        converter.setTrustedPackages("com.gym.workload.dto");
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of("trainer-workload-event", TrainerWorkloadRequest.class));
        return converter;
    }

}
