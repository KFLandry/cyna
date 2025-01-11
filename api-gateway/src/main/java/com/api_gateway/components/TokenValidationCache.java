package com.api_gateway.components;

import com.api_gateway.dto.ValidationResult;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;


@Component
public class TokenValidationCache {
    private final Cache<String, ValidationResult> cache;

    public TokenValidationCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }

    public Optional<ValidationResult> getValidationResult(String token) {
        return Optional.ofNullable(cache.getIfPresent(token));
    }

    public void cache(String token, ValidationResult result) {
        cache.put(token, result);
    }
}
