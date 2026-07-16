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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    @Column(nullable = false)
    private Integer xp = 0;

    @Column(nullable = false)
    private int level = 1;

    @Column(name = "fit_coins", nullable = false)
    private Integer fitcoins = 0;

    @Column(nullable = false)
    private Integer streak = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(nullable = false, length = 50, name = "avatar_border")
    private String avatarBorder = "DEFAULT";

    @Column(nullable = false, length = 50, name = "prestige_title")
    private String prestigeTitle = "NOVATO";

    // ADICIONE ESTE BLOCO AQUI NO FINAL DA SUA CLASSE:
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAchievement> userAchievements = new ArrayList<>();
}