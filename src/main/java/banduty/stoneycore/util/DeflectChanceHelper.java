package banduty.stoneycore.util;

import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class DeflectChanceHelper {
    private static final Random random = new Random();

    public static boolean shouldDeflect(LivingEntity livingEntity, ItemStack itemStack) {
        double deflectProbability = calculateDeflectProbability(livingEntity, itemStack);
        double random2 = random.nextDouble();
        return deflectProbability > random2;
    }

    private static double calculateDeflectProbability(LivingEntity livingEntity, ItemStack itemStack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        String itemKey = itemId.toString();
        double deflectChance = 0d;

        if (AccessoriesCapability.getOptionally(livingEntity).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(livingEntity).getAllEquipped()) {
                ItemStack equippedStack = equipped.stack();
                if (AccessoriesDefinitionsLoader.containsItem(equippedStack)) {
                    deflectChance += AccessoriesDefinitionsLoader.getData(equippedStack).deflectChance().getOrDefault(itemKey, 0.0);
                    if (AccessoriesDefinitionsLoader.getData(equippedStack).armorSlot().equals(EquipmentSlot.HEAD.getName()) &&
                            NBTDataHelper.get(equippedStack, INBTKeys.VISOR_OPEN, false)) {
                        deflectChance -= 0.05d;
                    }
                }
            }
        }

        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (ArmorDefinitionsLoader.containsItem(armorStack)) {
                deflectChance += ArmorDefinitionsLoader.getData(armorStack).deflectChance().getOrDefault(itemKey, 0.0);
            }
        }

        if (WeaponDefinitionsLoader.isMelee(itemStack)) {
            deflectChance += WeaponDefinitionsLoader.getData(itemStack).melee().deflectChance();
        }

        return deflectChance;
    }
}
