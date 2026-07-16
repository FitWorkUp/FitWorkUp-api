package com.fitworkup.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; 

    @Column(nullable = false)
    private String description; 

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward; 

    @Column(name = "fit_coins_reward", nullable = false)
    private Integer fitCoinsReward; 

    @Column(name = "icon_name", length = 50)
    private String iconName; 
}