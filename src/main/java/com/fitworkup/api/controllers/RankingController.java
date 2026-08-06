package com.fitworkup.controller;

import com.fitworkup.dto.response.RankingItemDTO;
import com.fitworkup.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public ResponseEntity<List<RankingItemDTO>> getTop10Ranking() {
        return ResponseEntity.ok(rankingService.getTop10Ranking());
    }
}