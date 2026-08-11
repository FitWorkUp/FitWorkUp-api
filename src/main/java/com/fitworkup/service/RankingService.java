package com.fitworkup.service;

import com.fitworkup.dto.response.RankingItemDTO;
import com.fitworkup.dto.response.WeeklyRankingResponseDTO;
import com.fitworkup.models.Activity;
import com.fitworkup.models.User;
import com.fitworkup.repository.ActivityRepository;
import com.fitworkup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RankingService {

    private static final int STEPS_PER_POINT = 100;

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    public RankingService(UserRepository userRepository,
                          ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public WeeklyRankingResponseDTO getWeeklyRanking(String currentIdentifier) {
        User currentUser = userRepository.findByEmailOrUsername(currentIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime startInclusive = weekStart.atStartOfDay();
        LocalDateTime endExclusive = weekStart.plusWeeks(1).atStartOfDay();

        Map<Long, WeeklyUserStats> statsByUser = new HashMap<>();
        for (Activity activity : activityRepository.findValidActivitiesBetween(startInclusive, endExclusive)) {
            User user = activity.getUser();
            WeeklyUserStats stats = statsByUser.computeIfAbsent(
                    user.getId(),
                    ignored -> new WeeklyUserStats(user)
            );
            stats.add(activity);
        }

        statsByUser.computeIfAbsent(currentUser.getId(), ignored -> new WeeklyUserStats(currentUser));

        List<WeeklyUserStats> orderedStats = new ArrayList<>(statsByUser.values());
        orderedStats.sort(
                Comparator.comparingLong(WeeklyUserStats::validatedSteps).reversed()
                        .thenComparing(Comparator.comparingInt(WeeklyUserStats::activeDays).reversed())
                        .thenComparing(stats -> stats.user().getUsername(), String.CASE_INSENSITIVE_ORDER)
        );

        List<RankingItemDTO> entries = new ArrayList<>();
        for (int index = 0; index < orderedStats.size(); index++) {
            WeeklyUserStats stats = orderedStats.get(index);
            User user = stats.user();
            entries.add(RankingItemDTO.builder()
                    .userId(user.getId())
                    .position(index + 1)
                    .username(user.getUsername())
                    .level(user.getLevel())
                    .validatedSteps(stats.validatedSteps())
                    .movementPoints(stats.validatedSteps() / STEPS_PER_POINT)
                    .activeDays(stats.activeDays())
                    .avatarBorder(user.getAvatarBorder())
                    .prestigeTitle(user.getPrestigeTitle())
                    .currentUser(user.getId().equals(currentUser.getId()))
                    .build());
        }

        return WeeklyRankingResponseDTO.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .stepsPerPoint(STEPS_PER_POINT)
                .entries(entries)
                .build();
    }

    private static final class WeeklyUserStats {
        private final User user;
        private long validatedSteps;
        private final Set<LocalDate> activeDates = new HashSet<>();

        private WeeklyUserStats(User user) {
            this.user = user;
        }

        private void add(Activity activity) {
            int acceptedSteps = activity.getAcceptedSteps() != null
                    ? activity.getAcceptedSteps()
                    : activity.getSteps() != null ? activity.getSteps() : 0;
            validatedSteps += Math.max(acceptedSteps, 0);
            activeDates.add(activity.getTimestamp().toLocalDate());
        }

        private User user() {
            return user;
        }

        private long validatedSteps() {
            return validatedSteps;
        }

        private int activeDays() {
            return activeDates.size();
        }
    }
}
