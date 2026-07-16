package com.fitworkup.models;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
@Table(name = "store_items")
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, length = 50) // COSMETIC ou CONSUMABLE
    private String type;
}