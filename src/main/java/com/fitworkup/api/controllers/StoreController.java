package com.fitworkup.api.controllers;

import com.fitworkup.dto.response.InventoryItemResponseDTO;
import com.fitworkup.dto.response.ActiveBoostResponseDTO;
import com.fitworkup.dto.response.PurchaseResponseDTO;
import com.fitworkup.dto.response.StoreItemResponseDTO;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.security.exceptions.ResourceNotFoundException;
import com.fitworkup.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {

    private final StoreService storeService;
    private final UserRepository userRepository;

    public StoreController(StoreService storeService, UserRepository userRepository) {
        this.storeService = storeService;
        this.userRepository = userRepository;
    }

    @GetMapping("/items")
    public ResponseEntity<List<StoreItemResponseDTO>> getAllItems(
            @RequestParam(required = false) String category
    ) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(storeService.getItemsByCategory(category));
        }
        return ResponseEntity.ok(storeService.getAllItems());
    }

    @PostMapping("/purchase/{storeItemId}")
    public ResponseEntity<PurchaseResponseDTO> purchaseItem(
            @PathVariable Long storeItemId,
            Principal principal
    ) {
        User user = authenticatedUser(principal);
        return ResponseEntity.ok(storeService.purchaseItem(user.getId(), storeItemId));
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryItemResponseDTO>> getUserInventory(Principal principal) {
        User user = authenticatedUser(principal);
        return ResponseEntity.ok(storeService.getUserInventory(user.getId()));
    }

    @GetMapping("/boosts/active")
    public ResponseEntity<List<ActiveBoostResponseDTO>> getActiveBoosts(Principal principal) {
        User user = authenticatedUser(principal);
        return ResponseEntity.ok(storeService.getActiveBoosts(user.getId()));
    }

    @PostMapping("/equip/{inventoryItemId}")
    public ResponseEntity<InventoryItemResponseDTO> equipItem(
            @PathVariable Long inventoryItemId,
            Principal principal
    ) {
        User user = authenticatedUser(principal);
        return ResponseEntity.ok(storeService.equipItem(user.getId(), inventoryItemId));
    }

    private User authenticatedUser(Principal principal) {
        return userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }
}
