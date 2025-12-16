package com.ecommerce.user.controller;

import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.security.JwtUtil;
import com.ecommerce.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            
            // ✅ Generate token with userId included
            String token = jwtUtil.generateToken(
                registeredUser.getId(),           // ✅ userId
                registeredUser.getUsername(),
                registeredUser.getRole()
            );
            
            // ✅ Return success response with token and user info
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("token", token);
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
            response.put("role", registeredUser.getRole());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {

        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        // ✅ Get user details to extract role and id
        User user = userRepository.findByUsername(request.getUsername());
        
        // ✅ Generate token WITH userId and role
        String token = jwtUtil.generateToken(
            user.getId(),          // ✅ userId
            user.getUsername(),
            user.getRole()
        );
        
        // ✅ Return token and user info
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("name", user.getName());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing token"));
        }

        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);
        
        // ✅ Extract role and userId from token
        String role = jwtUtil.extractRole(token);
        Integer userId = jwtUtil.extractUserId(token);

        User user = userRepository.findByUsername(username);

        // ✅ Return user info without password
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("userId", userId);  // From token
        response.put("name", user.getName());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }
}