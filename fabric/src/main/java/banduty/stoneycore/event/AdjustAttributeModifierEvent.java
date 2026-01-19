package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class AdjustAttributeModifierEvent implements AdjustAttributeModifierCallback {
    @Override
    public void adjustAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        updatePlayerAttributes(stack, reference, builder);
    }

    private static void handleAttribute(SlotReference reference, Attribute attribute, String name, double value, AccessoryAttributeBuilder builder) {
        AttributeInstance instance = reference.entity().getAttribute(attribute);
        if (instance == null) return;

        builder.addStackable(attribute, new ResourceLocation(StoneyCore.MOD_ID, name), value, AttributeModifier.Operation.ADDITION);
    }

    private static void updatePlayerAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        double armor = 0;
        double toughness = 0;
        if (AccessoriesDefinitionsStorage.containsItem(stack)) {
            var data = AccessoriesDefinitionsStorage.getData(stack);
            armor = data.armor();
            toughness = data.toughness();
            handleAttribute(reference, Services.ATTRIBUTES.getHungerDrainMultiplier(), "hunger_drain_multiplier", data.hungerDrainMultiplier(), builder);
        }
        if (NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)) {
            armor -= 1;
            toughness -= 1;
        }

        handleAttribute(reference, Attributes.ARMOR, "armor", armor, builder);
        handleAttribute(reference, Attributes.ARMOR_TOUGHNESS, "armor_toughness", toughness, builder);
    }
}
