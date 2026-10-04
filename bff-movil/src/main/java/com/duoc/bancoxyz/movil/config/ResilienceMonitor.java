package com.duoc.bancoxyz.movil.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ResilienceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(ResilienceMonitor.class);
    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> circuitEvents() {
        Set<String> observed = ConcurrentHashMap.newKeySet();
        return factory -> factory.addCircuitBreakerCustomizer(circuit -> {
            if (observed.add(circuit.getName())) {
                circuit.getEventPublisher().onStateTransition(event ->
                    LOG.info("CIRCUITO bancoApi {}", event.getStateTransition()));
            }
        }, "bancoApi");
    }
}
