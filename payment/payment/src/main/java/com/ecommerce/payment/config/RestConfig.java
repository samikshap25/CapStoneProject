package com.ecommerce.payment.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    @Bean
    @LoadBalanced  // ✅ ADD THIS FOR EUREKA SERVICE DISCOVERY
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}