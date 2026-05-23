package banduty.stoneycore.util;

import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class WeightUtil {
    private static final Int2DoubleOpenHashMap ENTITY_WEIGHTS = new Int2DoubleOpenHashMap();

    static {
        ENTITY_WEIGHTS.defaultReturnValue(-1.0);
    }

    private static double calculateWeight(LivingEntity livingEntity) {
        double weight = 0.0;

        for (ItemStack itemStack : livingEntity.getArmorSlots()) {
            for (ItemStack armorAttachment : SCUnderArmor.getArmorAttachments(itemStack)) {
                if (!armorAttachment.isEmpty()) {
                    var data = ArmorAttachmentDefinitionsStorage.getData(armorAttachment);
                    if (data != null) weight += data.weight();
                }
            }
        }

        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (!armorStack.isEmpty()) {
                var data = ArmorDefinitionsStorage.getData(armorStack);
                if (data != null) weight += data.weight();
            }
        }
        return weight;
    }

    public static double getWeight(LivingEntity entity) {
        double cached = ENTITY_WEIGHTS.get(entity.getId());
        if (cached == -1.0) {
            cached = calculateWeight(entity);
            ENTITY_WEIGHTS.put(entity.getId(), cached);
        }
        return cached;
    }

    public static void refreshWeight(LivingEntity entity) {
        ENTITY_WEIGHTS.put(entity.getId(), calculateWeight(entity));
    }

    public static void removeEntity(LivingEntity entity) {
        ENTITY_WEIGHTS.remove(entity.getId());
    }
}