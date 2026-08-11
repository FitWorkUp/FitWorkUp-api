package com.fitworkup.models;

import com.fitworkup.enums.StoreEffectType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_store_item_name", columnNames = "name")
})
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
    private Integer price;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 255)
    private String description = "";

    @Column(nullable = false, length = 16)
    private String iconEmoji = "🎁";

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean repeatable = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StoreEffectType effectType;

    private Double multiplier;

    private Integer durationMinutes;

    @PrePersist
    @PreUpdate
    void applyDefaults() {
        if (description == null) description = "";
        if (iconEmoji == null || iconEmoji.isBlank()) iconEmoji = "🎁";
        if (active == null) active = true;
        if (repeatable == null) repeatable = false;
    }
}
