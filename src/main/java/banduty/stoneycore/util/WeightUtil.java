package banduty.stoneycore.util;

import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WeightUtil {
    private static final Map<Integer, Double> ENTITY_WEIGHTS = new HashMap<>();

    private static double calculateWeight(LivingEntity livingEntity) {
        double weight = 0f;

        if (AccessoriesCapability.getOptionally(livingEntity).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(livingEntity).getAllEquipped()) {
                ItemStack equippedStack = equipped.stack();
                if (AccessoriesDefinitionsLoader.containsItem(equippedStack)) {
                    weight += AccessoriesDefinitionsLoader.getData(equippedStack).weight();
                }
            }
        }

        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (ArmorDefinitionsLoader.containsItem(armorStack)) {
                weight += ArmorDefinitionsLoader.getData(armorStack).weight();
            }
        }

        return weight;
    }

    public static double getCachedWeight(LivingEntity entity) {
        return ENTITY_WEIGHTS.getOrDefault(entity.getId(), 0.0);
    }

    public static void setCachedWeight(LivingEntity entity) {
        ENTITY_WEIGHTS.put(entity.getId(), calculateWeight(entity));
    }

    public static void clearCache() {
        ENTITY_WEIGHTS.clear();
    }
}
