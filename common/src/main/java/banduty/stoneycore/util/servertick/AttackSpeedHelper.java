package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class AttackSpeedHelper {
    private static final ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "attack_speed_change");

    public static void changeAttackSpeed(LivingEntity livingEntity) {
        AttributeInstance attackSpeedAttr = livingEntity.getAttribute(Attributes.ATTACK_SPEED);

        if (attackSpeedAttr != null) {
            attackSpeedAttr.removeModifier(resourceLocation);

            float dataValue = 0;
            for (ItemStack itemStack : livingEntity.getArmorSlots()) {
                for (ItemStack attachment : SCUnderArmor.getArmorAttachments(itemStack)) {
                    dataValue += ArmorAttachmentDefinitionsStorage.getData(attachment).attackSpeed();
                }
            }

            AttributeModifier modifier = new AttributeModifier(
                    resourceLocation,
                    dataValue,
                    AttributeModifier.Operation.ADD_VALUE
            );

            attackSpeedAttr.addTransientModifier(modifier);
        }
    }

    public static int getReloadSpeedModified(LivingEntity livingEntity, ItemStack rangedItem) {
        int rechargeTime = WeaponDefinitionsStorage.getData(rangedItem).ranged().rechargeTime();
        for (ItemStack itemStack : livingEntity.getArmorSlots()) {
            for (ItemStack attachment : SCUnderArmor.getArmorAttachments(itemStack)) {
                rechargeTime += ArmorAttachmentDefinitionsStorage.getData(attachment).rechargeTime();
            }
        }
        return rechargeTime;
    }
}
