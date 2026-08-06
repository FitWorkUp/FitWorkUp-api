package com.fitworkup.service;

import com.fitworkup.models.InventoryItem;
import com.fitworkup.models.StoreItem;
import com.fitworkup.models.User;
import com.fitworkup.repository.InventoryItemRepository;
import com.fitworkup.repository.StoreItemRepository;
import com.fitworkup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreService {

    private final StoreItemRepository storeItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;

    public StoreService(StoreItemRepository storeItemRepository,
                        InventoryItemRepository inventoryItemRepository,
                        UserRepository userRepository) {
        this.storeItemRepository = storeItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StoreItem> getAllItems() {
        return storeItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StoreItem> getItemsByCategory(String category) {
        return storeItemRepository.findByCategory(category);
    }

    @Transactional
    public InventoryItem purchaseItem(Long userId, Long storeItemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        StoreItem storeItem = storeItemRepository.findById(storeItemId)
                .orElseThrow(() -> new IllegalArgumentException("Item da loja não encontrado."));

        if (user.getFitcoins() < storeItem.getPrice()) {
            throw new IllegalStateException("Saldo de FitCoins insuficiente para adquirir este item.");
        }

        // Abate o saldo de moedas do usuário
        user.setFitcoins(user.getFitcoins() - storeItem.getPrice());
        userRepository.save(user);

        // Se o usuário já possui o item no inventário, incrementa a quantidade
        InventoryItem inventoryItem = inventoryItemRepository.findByUserIdAndStoreItemId(userId, storeItemId)
                .orElse(new InventoryItem(null, user, storeItem, 0, false));

        inventoryItem.setQuantity(inventoryItem.getQuantity() + 1);
        return inventoryItemRepository.save(inventoryItem);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getUserInventory(Long userId) {
        return inventoryItemRepository.findByUserIdWithStoreItem(userId);
    }

    @Transactional
    public void equipItem(Long userId, Long inventoryItemId) {
        InventoryItem targetItem = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new IllegalArgumentException("Item do inventário não encontrado."));

        if (!targetItem.getUser().getId().equals(userId)) {
            throw new SecurityException("Acesso negado a este item do inventário.");
        }

        User user = targetItem.getUser();
        String category = targetItem.getStoreItem().getCategory();

        // Se for borda de avatar ou título, atualiza as colunas diretas da tabela User
        if ("AVATAR_FRAME".equalsIgnoreCase(category)) {
            user.setAvatarBorder(targetItem.getStoreItem().getName());
        } else if ("TITLE".equalsIgnoreCase(category)) {
            user.setPrestigeTitle(targetItem.getStoreItem().getName());
        }

        targetItem.setIsEquipped(true);
        userRepository.save(user);
        inventoryItemRepository.save(targetItem);
    }
}