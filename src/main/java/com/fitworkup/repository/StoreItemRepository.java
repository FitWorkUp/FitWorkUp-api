package com.fitworkup.repository;

import com.fitworkup.models.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {

    List<StoreItem> findByCategory(String category);
    
}
