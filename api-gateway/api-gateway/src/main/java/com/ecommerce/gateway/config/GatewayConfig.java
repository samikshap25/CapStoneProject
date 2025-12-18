package com.ecommerce.gateway.config;

import com.ecommerce.gateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                
                // ==================================================
                // USER SERVICE ROUTES
                // ==================================================
                
                // 🔓 PUBLIC: Signup (no JWT required)
                .route("user-signup", r -> r
                        .path("/api/user/signup")
                        .and()
                        .method("POST")
                        .uri("lb://USER-SERVICE"))
                
                // 🔓 PUBLIC: Login/Token (no JWT required)
                .route("user-login", r -> r
                        .path("/api/user/token")
                        .and()
                        .method("GET")
                        .uri("lb://USER-SERVICE"))
                
                // 🔒 PROTECTED: User details (JWT required)
                .route("user-details", r -> r
                        .path("/api/user/details")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE"))
                
                // 🔒 PROTECTED: All other user endpoints
                .route("user-service", r -> r
                        .path("/api/user/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE"))
                
                // ==================================================
                // ORDER SERVICE ROUTES (All Protected)
                // ==================================================
                
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://ORDER-SERVICE"))
                
                // ==================================================
                // PRODUCT SERVICE ROUTES
                // ==================================================
                
                // 🔓 PUBLIC: View products (GET only, no JWT)
                .route("product-view", r -> r
                        .path("/api/products/**")
                        .and()
                        .method("GET")
                        .uri("lb://PRODUCT-SERVICE"))
                
                // 🔒 PROTECTED: Create/Update/Delete products (JWT required)
                .route("product-manage", r -> r
                        .path("/api/products/**")
                        .and()
                        .method("POST", "PUT", "DELETE", "PATCH")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://PRODUCT-SERVICE"))
                
                // ==================================================
                // INVENTORY SERVICE ROUTES (All Protected)
                // ==================================================
                
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://INVENTORY-SERVICE"))
                
                .build();
    }
}