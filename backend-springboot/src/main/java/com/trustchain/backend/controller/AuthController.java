package com.trustchain.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String role = body.getOrDefault("role", "MANUFACTURER");
        String username = body.getOrDefault("username", role.toLowerCase() + "-user");

        return ResponseEntity.ok(Map.of(
                "token", "demo-jwt-token-" + role.toLowerCase(),
                "username", username,
                "role", role,
                "organization", "Org1MSP"
        ));
    }
}
