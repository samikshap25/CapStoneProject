package com.ecommerce.user.controller;

import com.ecommerce.user.model.User;
import com.ecommerce.user.security.JwtUtil;
import com.ecommerce.user.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserService userService;

    @PostMapping("/signup")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {

        authManager.authenticate(
        new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        )
);

String token = jwtUtil.generateToken(request.getUsername());
return ResponseEntity.ok(Map.of("token", token));
    }
}