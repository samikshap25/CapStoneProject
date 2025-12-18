package com.ecommerce.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    /**
     * List of public endpoints that DON'T require JWT token
     */
    public static final List<String> openApiEndpoints = List.of(
            "/api/user/signup",
            "/api/user/token",
            "/eureka"
    );

    /**
     * Predicate to check if route is secured (requires JWT)
     * Returns TRUE if JWT is required, FALSE if public endpoint
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}