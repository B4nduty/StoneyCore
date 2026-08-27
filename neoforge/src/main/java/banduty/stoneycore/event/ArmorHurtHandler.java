package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.data.SCDataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ArmorHurtHandler {

    @SubscribeEvent
    public static void onArmorHurt(ArmorHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();

        for (var mapEntry : event.getArmorMap().entrySet()) {
            EquipmentSlot slot = mapEntry.getKey();
            ArmorHurtEvent.ArmorEntry armorEntry = mapEntry.getValue();

            ItemStack itemStack = armorEntry.armorItemStack;
            if (itemStack.isEmpty()) continue;

            if (!(itemStack.getItem() instanceof SCUnderArmor scUnderArmor)) {
                continue;
            }

            int durabilityDamage = Math.max(1, Math.round(armorEntry.newDamage));

            ArmorItem.Type armorType = scUnderArmor.getType();

            UnderArmorContents contents = itemStack.getOrDefault(
                    SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY);

            if (contents.isEmpty()) {
                event.setNewDamage(slot, 0f);
                continue;
            }

            UnderArmorContents.Mutable mutableContents = new UnderArmorContents.Mutable(contents);
            boolean slotProtected = mutableContents.damageAttachment(armorType, durabilityDamage, livingEntity);

            if (slotProtected) {
                UnderArmorContents newContents = mutableContents.toImmutable();

                if (newContents.isEmpty()) {
                    itemStack.remove(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
                } else {
                    itemStack.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), newContents);
                }

                scUnderArmor.rebuildAttachmentAttributes(itemStack);

                event.setNewDamage(slot, 0f);
            }
        }
    }
}