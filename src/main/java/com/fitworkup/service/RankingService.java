package com.fitworkup.service;

import com.fitworkup.dto.response.RankingItemDTO;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    private final UserRepository userRepository;

    public RankingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RankingItemDTO> getTop10Ranking() {
        List<User> topUsers = userRepository.findTop10ByOrderByXpDesc();
        List<RankingItemDTO> ranking = new ArrayList<>();

        for (int i = 0; i < topUsers.size(); i++) {
            User u = topUsers.get(i);
            ranking.add(RankingItemDTO.builder()
                    .userId(u.getId())
                    .position(i + 1)
                    .username(u.getUsername())
                    .level(u.getLevel())
                    .xp(u.getXp())
                    .avatarBorder(u.getAvatarBorder())
                    .prestigeTitle(u.getPrestigeTitle())
                    .build());
        }

        return ranking;
    }
}