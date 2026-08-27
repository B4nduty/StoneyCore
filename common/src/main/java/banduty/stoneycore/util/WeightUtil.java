package banduty.stoneycore.util;

import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.definitions.ArmorAttachmentDefinitionsStorage;
import banduty.stoneycore.definitions.ArmorDefinitionsStorage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class WeightUtil {
    private static final Map<LivingEntity, Double> ENTITY_WEIGHTS = Collections.synchronizedMap(new WeakHashMap<>());

    private static double calculateWeight(LivingEntity livingEntity) {
        double weight = 0.0;

        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (armorStack.isEmpty()) continue;

            var armorData = ArmorDefinitionsStorage.getData(armorStack);
            if (armorData != null) weight += armorData.weight();

            for (ItemStack attachment : SCUnderArmor.getArmorAttachments(armorStack)) {
                if (attachment.isEmpty()) continue;

                var attachmentData = ArmorAttachmentDefinitionsStorage.getData(attachment);
                if (attachmentData != null) weight += attachmentData.weight();
            }
        }

        return weight;
    }

    public static double getWeight(LivingEntity entity) {
        Double cached = ENTITY_WEIGHTS.get(entity);
        if (cached != null) {
            return cached;
        }

        double weight = calculateWeight(entity);
        ENTITY_WEIGHTS.put(entity, weight);
        return weight;
    }

    public static void refreshWeight(LivingEntity entity) {
        ENTITY_WEIGHTS.put(entity, calculateWeight(entity));
    }

    public static void removeEntity(LivingEntity entity) {
        ENTITY_WEIGHTS.remove(entity);
    }
}