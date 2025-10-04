package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsLoader;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AdjustAttributeModifierEvent implements AdjustAttributeModifierCallback {
    @Override
    public void adjustAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        if (!AccessoriesDefinitionsLoader.containsItem(stack)) return;
        updatePlayerAttributes(stack, reference, builder);
    }

    private static void handleAttribute(SlotReference reference, EntityAttribute attribute, String name, double value, AccessoryAttributeBuilder builder) {
        EntityAttributeInstance instance = reference.entity().getAttributeInstance(attribute);
        if (instance == null) return;

        builder.addStackable(attribute, new Identifier(StoneyCore.MOD_ID, name), value, EntityAttributeModifier.Operation.ADDITION);
    }

    private static void updatePlayerAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        var data = AccessoriesDefinitionsLoader.getData(stack);
        var armor = data.armor();
        var toughness = data.toughness();
        if (NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)) {
            armor -= 1;
            toughness -= 1;
        }

        handleAttribute(reference, EntityAttributes.GENERIC_ARMOR, "armor", armor, builder);
        handleAttribute(reference, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, "armor_toughness", toughness, builder);
        handleAttribute(reference, StoneyCore.HUNGER_DRAIN_MULTIPLIER.get(), "hunger_drain_multiplier", data.hungerDrainMultiplier(), builder);
    }
}
