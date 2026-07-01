package com.fitworkup.api.models;

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
    private String name; // Ex: "Monstro dos Passos", "Madrugador"

    @Column(nullable = false)
    private String description; // Ex: "Complete um treino com mais de 10.000 passos"

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward; // Quanto de XP o usuário ganha (ex: 150)

    @Column(name = "fit_coins_reward", nullable = false)
    private Integer fitCoinsReward; // Quantas FitCoins ele ganha (ex: 30)

    @Column(name = "icon_name", length = 50)
    private String iconName; // Nome do ícone para o Jetpack Compose carregar (ex: "ic_fire", "ic_trophy")
}