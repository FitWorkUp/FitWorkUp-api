package com.fitworkup.service;

import com.fitworkup.dto.response.InventoryItemResponseDTO;
import com.fitworkup.dto.response.ActiveBoostResponseDTO;
import com.fitworkup.dto.response.PurchaseResponseDTO;
import com.fitworkup.dto.response.StoreItemResponseDTO;
import com.fitworkup.models.InventoryItem;
import com.fitworkup.models.StoreItem;
import com.fitworkup.models.User;
import com.fitworkup.models.UserBoost;
import com.fitworkup.repository.InventoryItemRepository;
import com.fitworkup.repository.StoreItemRepository;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.repository.UserBoostRepository;
import com.fitworkup.security.exceptions.BusinessRuleException;
import com.fitworkup.security.exceptions.InsufficientFundsException;
import com.fitworkup.security.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StoreService {

    private final StoreItemRepository storeItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;
    private final UserBoostRepository userBoostRepository;

    public StoreService(StoreItemRepository storeItemRepository,
                        InventoryItemRepository inventoryItemRepository,
                        UserRepository userRepository,
                        UserBoostRepository userBoostRepository) {
        this.storeItemRepository = storeItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.userRepository = userRepository;
        this.userBoostRepository = userBoostRepository;
    }

    @Transactional(readOnly = true)
    public List<StoreItemResponseDTO> getAllItems() {
        return storeItemRepository.findByActiveTrueOrderByPriceAsc().stream()
                .map(this::toStoreItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StoreItemResponseDTO> getItemsByCategory(String category) {
        return storeItemRepository
                .findByCategoryIgnoreCaseAndActiveTrueOrderByPriceAsc(category)
                .stream()
                .map(this::toStoreItemResponse)
                .toList();
    }

    @Transactional
    public PurchaseResponseDTO purchaseItem(Long userId, Long storeItemId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        StoreItem storeItem = storeItemRepository.findById(storeItemId)
                .filter(item -> Boolean.TRUE.equals(item.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Item da loja não encontrado."));

        boolean isBoost = "BOOST".equalsIgnoreCase(storeItem.getCategory());
        InventoryItem inventoryItem = isBoost ? null : inventoryItemRepository
                .findByUserIdAndStoreItemId(userId, storeItemId).orElse(null);

        if (inventoryItem != null && !Boolean.TRUE.equals(storeItem.getRepeatable())) {
            throw new BusinessRuleException("Você já possui este item.");
        }

        int currentBalance = user.getFitcoins() != null ? user.getFitcoins() : 0;
        if (currentBalance < storeItem.getPrice()) {
            throw new InsufficientFundsException(
                    "Saldo insuficiente. Você possui " + currentBalance +
                    " FC e o item custa " + storeItem.getPrice() + " FC."
            );
        }

        user.setFitcoins(currentBalance - storeItem.getPrice());
        userRepository.save(user);

        InventoryItem savedItem = null;
        Instant boostExpiresAt = null;
        if (isBoost) {
            boostExpiresAt = activateBoost(user, storeItem);
        } else {
            if (inventoryItem == null) {
                inventoryItem = new InventoryItem(null, user, storeItem, 1, false);
            } else {
                inventoryItem.setQuantity(inventoryItem.getQuantity() + 1);
            }
            savedItem = inventoryItemRepository.save(inventoryItem);
        }

        return new PurchaseResponseDTO(
                savedItem != null ? savedItem.getId() : null,
                storeItem.getId(),
                savedItem != null ? savedItem.getQuantity() : null,
                user.getFitcoins(),
                isBoost ? "Bônus ativado com sucesso." : "Compra realizada com sucesso.",
                storeItem.getRepeatable(),
                boostExpiresAt
        );
    }

    @Transactional(readOnly = true)
    public List<ActiveBoostResponseDTO> getActiveBoosts(Long userId) {
        return userBoostRepository
                .findByUserIdAndExpiresAtAfterOrderByExpiresAtAsc(userId, Instant.now())
                .stream()
                .map(boost -> new ActiveBoostResponseDTO(
                        boost.getEffectType().name(),
                        boost.getMultiplier(),
                        boost.getExpiresAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponseDTO> getUserInventory(Long userId) {
        return inventoryItemRepository.findByUserIdWithStoreItem(userId).stream()
                .map(this::toInventoryResponse)
                .toList();
    }

    @Transactional
    public InventoryItemResponseDTO equipItem(Long userId, Long inventoryItemId) {
        InventoryItem targetItem = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item do inventário não encontrado."));

        if (!targetItem.getUser().getId().equals(userId)) {
            throw new BusinessRuleException("Este item não pertence ao usuário autenticado.");
        }

        String category = targetItem.getStoreItem().getCategory();
        User user = targetItem.getUser();

        if ("AVATAR_FRAME".equalsIgnoreCase(category)) {
            user.setAvatarBorder(targetItem.getStoreItem().getName());
        } else if ("TITLE".equalsIgnoreCase(category)) {
            user.setPrestigeTitle(targetItem.getStoreItem().getName());
        } else {
            throw new BusinessRuleException("Este tipo de item não pode ser equipado.");
        }

        inventoryItemRepository.findEquippedByUserIdAndCategory(userId, category)
                .forEach(item -> item.setIsEquipped(false));
        targetItem.setIsEquipped(true);

        userRepository.save(user);
        inventoryItemRepository.save(targetItem);
        return toInventoryResponse(targetItem);
    }

    private StoreItemResponseDTO toStoreItemResponse(StoreItem item) {
        return new StoreItemResponseDTO(
                item.getId(), item.getName(), item.getDescription(), item.getPrice(),
                item.getCategory(), item.getIconEmoji(), item.getRepeatable(),
                item.getEffectType() != null ? item.getEffectType().name() : null,
                item.getMultiplier(), item.getDurationMinutes()
        );
    }

    private Instant activateBoost(User user, StoreItem storeItem) {
        if (storeItem.getEffectType() == null || storeItem.getMultiplier() == null ||
                storeItem.getDurationMinutes() == null || storeItem.getDurationMinutes() <= 0) {
            throw new BusinessRuleException("O bônus está configurado incorretamente.");
        }

        Instant now = Instant.now();
        UserBoost boost = userBoostRepository
                .findByUserIdAndEffectType(user.getId(), storeItem.getEffectType())
                .orElseGet(UserBoost::new);
        Instant extensionStart = boost.getExpiresAt() != null && boost.getExpiresAt().isAfter(now)
                ? boost.getExpiresAt()
                : now;

        boost.setUser(user);
        boost.setStoreItem(storeItem);
        boost.setEffectType(storeItem.getEffectType());
        boost.setMultiplier(storeItem.getMultiplier());
        boost.setStartsAt(now);
        boost.setExpiresAt(extensionStart.plus(storeItem.getDurationMinutes(), ChronoUnit.MINUTES));
        return userBoostRepository.save(boost).getExpiresAt();
    }

    private InventoryItemResponseDTO toInventoryResponse(InventoryItem inventoryItem) {
        StoreItem item = inventoryItem.getStoreItem();
        return new InventoryItemResponseDTO(
                inventoryItem.getId(), item.getId(), item.getName(), item.getDescription(),
                item.getPrice(), item.getCategory(), item.getIconEmoji(),
                inventoryItem.getQuantity(), inventoryItem.getIsEquipped()
        );
    }
}
