package com.api_gateway.components;

import com.api_gateway.dto.TokenValidationRequest;
import com.api_gateway.dto.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenValidationClient {

    @Value("${spring.cloud.gateway.routes[0].id}")
    private String serviceId;
    private final DiscoveryClient discoveryClient;
    private final RestClient.Builder restClientBuilder;

    public ValidationResult validate(String token) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            if (instances.isEmpty()) {
                log.error("No instances found for service: {}", serviceId);
                throw new RuntimeException("Auth service not available");
            }

            ServiceInstance serviceInstance = instances.getFirst();
            log.debug("Calling auth service at: {}", serviceInstance.getUri());

            return restClientBuilder.build()
                    .post()
                    .uri(serviceInstance.getUri() + "/api/v1/auth/validate")
                    .body(new TokenValidationRequest(token))
                    .retrieve()
                    .body(ValidationResult.class);
        } catch (Exception e) {
            log.error("Error validating token", e);
            throw new RuntimeException("Token validation failed", e);
        }
    }
}