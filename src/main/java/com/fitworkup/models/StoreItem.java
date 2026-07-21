package com.fitworkup.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer price; // Custo em moedas (Coins) do FitWorkUp

    // Adicionando o campo que o repositório estava cobrando
    @Column(nullable = false, length = 50)
    private String category; // EX: "COSMETIC", "AVATAR_FRAME", "XP_BOOST"
}