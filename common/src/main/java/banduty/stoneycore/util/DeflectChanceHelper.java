package banduty.stoneycore.util;

import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
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

        for (ItemStack equippedStack : Services.PLATFORM.getEquippedAccessories(livingEntity)) {
            if (AccessoriesDefinitionsStorage.containsItem(equippedStack)) {
                deflectChance += AccessoriesDefinitionsStorage.getData(equippedStack).deflectChance().getOrDefault(itemKey, 0.0);
                if (AccessoriesDefinitionsStorage.getData(equippedStack).armorSlot().equals(EquipmentSlot.HEAD.getName()) &&
                        NBTDataHelper.get(equippedStack, INBTKeys.VISOR_OPEN, false)) {
                    deflectChance -= 0.05d;
                }
            }
        }

        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(armorStack)) {
                deflectChance += ArmorDefinitionsStorage.getData(armorStack).deflectChance().getOrDefault(itemKey, 0.0);
            }
        }

        if (WeaponDefinitionsStorage.isMelee(itemStack)) {
            deflectChance += WeaponDefinitionsStorage.getData(itemStack).melee().deflectChance();
        }

        return deflectChance;
    }
}
