package com.fitworkup.models;

import com.fitworkup.enums.StoreEffectType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_boosts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_boost_effect", columnNames = {"user_id", "effect_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBoost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_item_id", nullable = false)
    private StoreItem storeItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false, length = 50)
    private StoreEffectType effectType;

    @Column(nullable = false)
    private Double multiplier;

    @Column(nullable = false)
    private Instant startsAt;

    @Column(nullable = false)
    private Instant expiresAt;
}
