package com.greenthumb.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for the {@link RestTemplate} used to call external APIs.
 *
 * @author Hamza Ali
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a default {@link RestTemplate} bean for HTTP client operations.
     *
     * @return a new RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
