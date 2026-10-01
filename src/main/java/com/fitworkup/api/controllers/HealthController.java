package com.fitworkup.api.controllers;
    
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, String>> checkHealth() {
        // Resposta imediata em memória (HTTP 200 OK)
        return ResponseEntity.ok(Collections.singletonMap("status", "UP"));
    }
}