package com.fitworkup.config;

import com.fitworkup.enums.StoreEffectType;
import com.fitworkup.models.StoreItem;
import com.fitworkup.repository.StoreItemRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevelopmentStoreDataConfig implements ApplicationRunner {

    private final StoreItemRepository storeItemRepository;

    public DevelopmentStoreDataConfig(StoreItemRepository storeItemRepository) {
        this.storeItemRepository = storeItemRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        createCosmetic(
                "Moldura Rubi", 10,
                "Uma moldura vermelha para destacar seu perfil.", "\uD83D\uDD34"
        );
        createCosmetic(
                "Moldura Ametista", 10,
                "Uma moldura roxa para destacar seu perfil.", "\uD83D\uDFE3"
        );   
        createCosmetic(
                "Moldura Esmeralda", 10,
                "Uma moldura verde desbloqueada com FitCoins.", "\uD83D\uDFE2"
        );
        createCosmetic(
                "Moldura Lendária", 50,
                "A moldura dourada mais rara da loja.", "\uD83D\uDFE1"
        );
        createBoost(
                "2x XP por 30 min", 50,
                "Dobra o XP recebido em atividades durante 30 minutos.", "\u26A1",
                StoreEffectType.XP_MULTIPLIER, 2.0, 30
        );
        createBoost(
                "2x FitCoins por 30 min", 70,
                "Dobra os FitCoins recebidos em atividades durante 30 minutos.", "\uD83E\uDE99",
                StoreEffectType.FITCOINS_MULTIPLIER, 2.0, 30
        );
    }

    private void createCosmetic(String name, int price, String description, String iconEmoji) {
        if (storeItemRepository.existsByNameIgnoreCase(name)) return;

        StoreItem item = baseItem(name, price, "AVATAR_FRAME", description, iconEmoji);
        item.setRepeatable(false);
        storeItemRepository.save(item);
    }

    private void createBoost(String name,
                             int price,
                             String description,
                             String iconEmoji,
                             StoreEffectType effectType,
                             double multiplier,
                             int durationMinutes) {
        if (storeItemRepository.existsByNameIgnoreCase(name)) return;

        StoreItem item = baseItem(name, price, "BOOST", description, iconEmoji);
        item.setRepeatable(true);
        item.setEffectType(effectType);
        item.setMultiplier(multiplier);
        item.setDurationMinutes(durationMinutes);
        storeItemRepository.save(item);
    }

    private StoreItem baseItem(
            String name,
            int price,
            String category,
            String description,
            String iconEmoji
    ) {
        StoreItem item = new StoreItem();
        item.setName(name);
        item.setPrice(price);
        item.setCategory(category);
        item.setDescription(description);
        item.setIconEmoji(iconEmoji);
        item.setActive(true);
        return item;
    }
}
