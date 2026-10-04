package com.duoc.bancoxyz.cajeros.service;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class BancoClient {
    private final RestClient api;
    private final DiscoveryClient discovery;
    private final CircuitBreaker circuit;

    public BancoClient(RestClient api, DiscoveryClient discovery,
                       Resilience4JCircuitBreakerFactory factory) {
        this.api = api;
        this.discovery = discovery;
        this.circuit = factory.create("bancoApi");
    }

    public JsonNode get(String path, String cuentaId) {
        var authentication = (JwtAuthenticationToken)
            SecurityContextHolder.getContext().getAuthentication();
        // Se captura antes del TimeLimiter: su tarea puede ejecutarse en otro hilo.
        String token = authentication.getToken().getTokenValue();
        return circuit.run(() -> {
            var instances = discovery.getInstances("banco-api");
            if (instances.isEmpty()) {
                throw new ResourceAccessException("banco-api no esta disponible en Eureka");
            }
            var uri = UriComponentsBuilder.fromUriString(instances.get(0).getUri().toString()).path(path);
            if (cuentaId != null && !cuentaId.isBlank()) {
                uri.queryParam("cuentaId", cuentaId);
            }
            return api.get().uri(uri.build().encode().toUri())
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve().body(JsonNode.class);
        }, this::fallback);
    }

    private JsonNode fallback(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof HttpClientErrorException clientError) {
                throw new ResponseStatusException(clientError.getStatusCode(),
                    "La API rechazo la solicitud");
            }
        }
        throw new BancoNoDisponibleException(failure);
    }
}
