package com.fitworkup.api.controllers;

import com.fitworkup.models.InventoryItem;
import com.fitworkup.models.StoreItem;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<StoreItem>> getAllItems(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(storeService.getItemsByCategory(category));
        }
        return ResponseEntity.ok(storeService.getAllItems());
    }

    @PostMapping("/purchase/{storeItemId}")
    public ResponseEntity<InventoryItem> purchaseItem(@PathVariable Long storeItemId, Principal principal) {
        User user = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        InventoryItem item = storeService.purchaseItem(user.getId(), storeItemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryItem>> getUserInventory(Principal principal) {
        User user = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        return ResponseEntity.ok(storeService.getUserInventory(user.getId()));
    }

    @PostMapping("/equip/{inventoryItemId}")
    public ResponseEntity<Void> equipItem(@PathVariable Long inventoryItemId, Principal principal) {
        User user = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        storeService.equipItem(user.getId(), inventoryItemId);
        return ResponseEntity.ok().build();
    }
}