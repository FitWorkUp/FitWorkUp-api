package com.fitworkup.repository;

import com.fitworkup.models.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {
    
    // Agora o Spring Data encontra a propriedade 'category' dentro de StoreItem e monta o SQL correto
    List<StoreItem> findByCategory(String category);
}