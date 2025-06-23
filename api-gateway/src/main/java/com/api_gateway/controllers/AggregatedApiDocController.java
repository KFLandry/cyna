package com.api_gateway.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AggregatedApiDocController {

    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    @GetMapping("/aggregated-api-docs")
    public Mono<ResponseEntity<JsonNode>> aggregateApiDocs() {
        List<Mono<JsonNode>> docsMonos = new ArrayList<>();

        // Pour chaque service découvert, interroge son endpoint /api-docs
        for (String serviceId : discoveryClient.getServices()) {
            if (!serviceId.contains("api-gateway")) {
                ServiceInstance instance = discoveryClient.getInstances(serviceId).getFirst();
                Mono<JsonNode> docMono = webClientBuilder.build()
                        .get()
                        .uri(instance.getServiceId() + "/api-docs")
                        .retrieve()
                        .bodyToMono(JsonNode.class);
                docsMonos.add(docMono);
            }
        }

        // Combine les résultats de tous les services dans un seul objet JSON
        return Mono.zip(docsMonos, results -> {
            ObjectNode aggregated = JsonNodeFactory.instance.objectNode();
            for (int i = 0; i < results.length; i++) {
                // Ici, on ajoute chaque document sous une clé "service-i"
                aggregated.set("service-" + i, (JsonNode) results[i]);
            }
            return aggregated;
        }).map(ResponseEntity::ok);
    }
}
