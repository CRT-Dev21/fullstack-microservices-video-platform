package dev.crt.api.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final String CATALOG_SERVICE_BASE_URL = "http://localhost:8082/api/v1/catalog";

    private static final String USER_SERVICE_BASE_URL = "http://localhost:8081/api/v1/users";

    @Bean
    public WebClient catalogServiceWebClient(){
        return WebClient.builder()
                .baseUrl(CATALOG_SERVICE_BASE_URL)
                .build();
    }

    @Bean
    public WebClient userServiceWebClient(){
        return WebClient.builder()
                .baseUrl(USER_SERVICE_BASE_URL)
                .build();
    }
}
