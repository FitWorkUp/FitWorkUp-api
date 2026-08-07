package com.fitworkup.repository;

import com.fitworkup.models.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.storeItem WHERE i.user.id = :userId")
    List<InventoryItem> findByUserIdWithStoreItem(@Param("userId") Long userId);

    List<InventoryItem> findByUserId(Long userId);

    Optional<InventoryItem> findByUserIdAndStoreItemId(Long userId, Long storeItemId);
}