package banduty.stoneycore.util;

import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WeightUtil {
    private static final Map<Integer, Double> ENTITY_WEIGHTS = new HashMap<>();

    private static double calculateWeight(LivingEntity livingEntity) {
        double weight = 0f;

        for (ItemStack equippedStack : Services.PLATFORM.getEquippedAccessories(livingEntity)) {
            if (AccessoriesDefinitionsStorage.containsItem(equippedStack)) {
                weight += AccessoriesDefinitionsStorage.getData(equippedStack).weight();
            }
        }

        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(armorStack)) {
                weight += ArmorDefinitionsStorage.getData(armorStack).weight();
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
