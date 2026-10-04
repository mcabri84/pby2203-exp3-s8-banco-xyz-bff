package com.duoc.bancoxyz.cajeros.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {
    @Bean
    RestClient bancoRestClient(@Value("${app.api.connect-timeout-ms}") int connectTimeout,
                              @Value("${app.api.read-timeout-ms}") int readTimeout) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return RestClient.builder().requestFactory(factory)
            .requestInterceptor((request, body, execution) -> {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof JwtAuthenticationToken jwt) {
                    request.getHeaders().setBearerAuth(jwt.getToken().getTokenValue());
                }
                return execution.execute(request, body);
            }).build();
    }
}
