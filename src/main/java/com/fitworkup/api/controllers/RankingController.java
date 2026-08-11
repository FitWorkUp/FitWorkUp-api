package com.fitworkup.api.controllers;

import com.fitworkup.dto.response.WeeklyRankingResponseDTO;
import com.fitworkup.service.RankingService;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklyRankingResponseDTO> getWeeklyRanking(Principal principal) {
        return ResponseEntity.ok(rankingService.getWeeklyRanking(principal.getName()));
    }
}
