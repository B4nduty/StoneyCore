package banduty.stoneycore.util;

import banduty.stoneycore.util.definitionsloader.SCAccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCWeaponDefinitionsLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Random;

public class DeflectChanceHelper {
    private static final Random random = new Random();

    public static boolean shouldDeflect(LivingEntity livingEntity, ItemStack itemStack) {
        double deflectProbability = calculateDeflectProbability(livingEntity, itemStack);
        double random2 = random.nextDouble();
        return random2 < deflectProbability;
    }

    private static double calculateDeflectProbability(LivingEntity livingEntity, ItemStack itemStack) {
        Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
        String itemKey = itemId.toString();
        double deflectChance = 0f;

        if (AccessoriesCapability.getOptionally(livingEntity).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(livingEntity).getAllEquipped()) {
                ItemStack equippedStack = equipped.stack();
                if (SCAccessoriesDefinitionsLoader.containsItem(equippedStack)) {
                    deflectChance += SCAccessoriesDefinitionsLoader.getData(equippedStack).deflectChance().getOrDefault(itemKey, 0.0);
                }
            }
        }

        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (SCArmorDefinitionsLoader.containsItem(armorStack)) {
                deflectChance += SCArmorDefinitionsLoader.getData(armorStack).deflectChance().getOrDefault(itemKey, 0.0);
            }
        }

        if (SCWeaponDefinitionsLoader.isMelee(itemStack)) {
            deflectChance += SCWeaponDefinitionsLoader.getData(itemStack).melee().deflectChance();
        }

        return deflectChance;
    }
}
