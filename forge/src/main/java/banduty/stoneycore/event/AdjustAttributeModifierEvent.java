package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AdjustAttributeModifierEvent {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        updatePlayerAttributes(event.getItemStack(), event);
    }

    private static void handleAttribute(ItemAttributeModifierEvent event, Attribute attribute, String name, double value) {
        if (value == 0 || attribute == null) return;

        event.addModifier(attribute, new AttributeModifier(
                UUID.nameUUIDFromBytes((StoneyCore.MOD_ID + ":" + name).getBytes()),
                StoneyCore.MOD_ID + "." + name,
                value,
                AttributeModifier.Operation.ADDITION
        ));
    }

    private static void updatePlayerAttributes(ItemStack stack, ItemAttributeModifierEvent event) {
        double armor = 0;
        double toughness = 0;

        if (AccessoriesDefinitionsStorage.containsItem(stack)) {
            var data = AccessoriesDefinitionsStorage.getData(stack);
            armor = data.armor();
            toughness = data.toughness();

            handleAttribute(event, Services.ATTRIBUTES.getHungerDrainMultiplier(),
                    "hunger_drain_multiplier", data.hungerDrainMultiplier());
        }

        if (NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)) {
            armor -= 1;
            toughness -= 1;
        }

        handleAttribute(event, Attributes.ARMOR, "armor", armor);
        handleAttribute(event, Attributes.ARMOR_TOUGHNESS, "armor_toughness", toughness);
    }
}