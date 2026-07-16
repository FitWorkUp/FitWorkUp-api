package com.fitworkup.repository;

import com.fitworkup.models.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByUserId(Long userId);

    // Encontra um item específico dentro do inventário do usuário (útil para atualizar quantidade ou equipar)
    Optional<InventoryItem> findByUserIdAndStoreItemId(Long userId, Long storeItemId);
}