package com.fitworkup.api.controllers;

import com.fitworkup.dto.response.RankingItemDTO;
import com.fitworkup.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/top10")
    public ResponseEntity<List<RankingItemDTO>> getTop10Ranking() {
        return ResponseEntity.ok(rankingService.getTop10Ranking());
    }
}