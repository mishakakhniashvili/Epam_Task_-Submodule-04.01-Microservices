package com.epam.gymcrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient trainerWorkloadRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${trainer-workload-service.base-url}") String baseUrl
    ) {
        return restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }
}