package com.ecommerce.order.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ✅ CRITICAL: Skip JWT validation for OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;
        Integer userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                username = jwtUtil.extractUsername(token);
                userId = jwtUtil.extractUserId(token); // ✅ Extract userId
                
                System.out.println("🔐 JWT Token parsed - Username: " + username + ", UserId: " + userId);
            } catch (Exception e) {
                System.err.println("JWT Error: " + e.getMessage());
            }
        }

        if (username != null && jwtUtil.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ✅ Store userId in request attribute for controllers to access
            if (userId != null) {
                request.setAttribute("userId", userId);
                System.out.println("✅ UserId " + userId + " stored in request attribute");
            } else {
                request.setAttribute("userId", 1); // Default fallback
                System.out.println("⚠️ No userId in token, using default: 1");
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            new org.springframework.security.core.userdetails.User(
                                    username, "", java.util.Collections.emptyList()
                            ),
                            null,
                            java.util.Collections.emptyList()
                    );

            auth.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}