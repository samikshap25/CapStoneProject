package com.ecommerce.user.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ✅ Enable Spring Boot managed CORS (NO manual bean)
            .cors(Customizer.withDefaults())

            // ❌ Disable CSRF (JWT based auth)
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // ✅ Allow CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ---------- PUBLIC AUTH ENDPOINTS ----------
                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/auth/token").permitAll()

                // ---------- PROTECTED AUTH ----------
                .requestMatchers("/api/auth/me").authenticated()

                // ---------- CUSTOMER ----------
                .requestMatchers("/api/users/me").hasAnyAuthority("CUSTOMER", "ADMIN")
                .requestMatchers("/api/users/update/me").hasAuthority("CUSTOMER")

                // ---------- ADMIN ----------
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/api/users/all").hasAuthority("ADMIN")
                .requestMatchers("/api/users/delete/**").hasAuthority("ADMIN")

                // ---------- EVERYTHING ELSE ----------
                .anyRequest().authenticated()
            )

            // ✅ JWT filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // (Optional) Basic auth disabled by default
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // ✅ Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ Authentication Manager
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
