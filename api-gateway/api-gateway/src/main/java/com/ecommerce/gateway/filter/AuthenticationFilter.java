package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if this route requires authentication
            if (routeValidator.isSecured.test(request)) {
                
                // Check if Authorization header exists
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                // Extract token from "Bearer <token>"
                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);

                try {
                    // Validate token
                    if (!jwtUtil.validateToken(token)) {
                        return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                    }

                    // Check if token is expired
                    if (jwtUtil.isTokenExpired(token)) {
                        return onError(exchange, "JWT token has expired", HttpStatus.UNAUTHORIZED);
                    }

                    // Extract username and add to request header
                    String username = jwtUtil.extractUsername(token);
                    request = exchange.getRequest()
                            .mutate()
                            .header("X-User-Name", username)
                            .build();

                } catch (Exception e) {
                    return onError(exchange, "JWT token validation failed", HttpStatus.UNAUTHORIZED);
                }
            }

            // Continue to next filter
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    /**
     * Handle error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
        // Configuration properties if needed
    }
}