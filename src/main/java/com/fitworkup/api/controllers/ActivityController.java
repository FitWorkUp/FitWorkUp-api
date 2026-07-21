package com.fitworkup.api.controllers;

import com.fitworkup.dto.request.ActivityRequest;
import com.fitworkup.models.Activity;
import com.fitworkup.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<Activity> registerActivity(
            @PathVariable Long userId,
            @RequestBody ActivityRequest request) {
        
        // O service aciona o anti-cheat, valida metas biométricas e calcula nível/moedas de forma atômica
        Activity registeredActivity = activityService.registerActivity(userId, request);
        
        // Retorna HTTP Status 201 (Created) junto com o corpo do registro persistido
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredActivity);
    }
}   