package com.api_gateway.components;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private final JwtAuthFilter jwtAuthFilter;

    public JwtAuthGatewayFilterFactory(JwtAuthFilter jwtAuthFilter) {
        super(Config.class);
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return jwtAuthFilter;
    }

    public static class Config {
    }
}
