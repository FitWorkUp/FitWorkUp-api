package com.fitworkup.api.controllers;

import com.fitworkup.dto.request.ActivityRequest;
import com.fitworkup.dto.response.DailySummaryResponse;
import com.fitworkup.dto.response.ActivityResponse;
import com.fitworkup.enums.ActivityStatus;
import com.fitworkup.models.Activity;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final UserRepository userRepository;

    public ActivityController(ActivityService activityService, UserRepository userRepository) {
        this.activityService = activityService;
        this.userRepository = userRepository;
    }

    @GetMapping("/today-summary")
    public ResponseEntity<DailySummaryResponse> getTodaySummary(Principal principal) {
        User user = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        return ResponseEntity.ok(activityService.getTodaySummary(user.getId()));
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> registerActivity(@Valid @RequestBody ActivityRequest request, Principal principal) {
        User user = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        Activity activity = activityService.registerActivity(user.getId(), request);
        ActivityStatus status = Boolean.TRUE.equals(activity.getIsValid())
                ? ActivityStatus.APPROVED
                : ActivityStatus.UNDER_REVIEW;
        return ResponseEntity.ok(ActivityResponse.fromEntity(activity, status));
    }
}
