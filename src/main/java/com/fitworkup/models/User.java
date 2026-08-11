package com.fitworkup.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(nullable = false)
    @Builder.Default
    private Integer xp = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "fit_coins", nullable = false)
    @Builder.Default
    private Integer fitcoins = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer streak = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(nullable = false, length = 50, name = "avatar_border")
    @Builder.Default
    private String avatarBorder = "DEFAULT";

    @Column(nullable = false, length = 50, name = "prestige_title")
    @Builder.Default
    private String prestigeTitle = "NOVATO";

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserAchievement> userAchievements = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (xp == null) xp = 0;
        if (level == null) level = 1;
        if (fitcoins == null) fitcoins = 0;
        if (streak == null) streak = 0;
        if (avatarBorder == null) avatarBorder = "DEFAULT";
        if (prestigeTitle == null) prestigeTitle = "NOVATO";
        if (active == null) active = true;
    }
}
