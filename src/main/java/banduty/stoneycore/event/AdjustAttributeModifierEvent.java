package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.definitionsloader.SCAccessoriesDefinitionsLoader;
import banduty.stoneycore.util.playerdata.SCAttributes;
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
        if (!SCAccessoriesDefinitionsLoader.containsItem(stack.getItem())) return;
        updatePlayerAttributes(stack, reference, builder);
    }

    private static void handleAttribute(SlotReference reference, EntityAttribute attribute, String name, double value, AccessoryAttributeBuilder builder) {
        EntityAttributeInstance instance = reference.entity().getAttributeInstance(attribute);
        if (instance == null) return;

        builder.addStackable(attribute, new Identifier(StoneyCore.MOD_ID, name), value, EntityAttributeModifier.Operation.ADDITION);
    }

    private static void updatePlayerAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        var data = SCAccessoriesDefinitionsLoader.getData(stack.getItem());

        handleAttribute(reference, EntityAttributes.GENERIC_ARMOR, "armor", data.armor(), builder);
        handleAttribute(reference, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, "armor_toughness", data.toughness(), builder);
        handleAttribute(reference, SCAttributes.HUNGER_DRAIN_MULTIPLIER, "hunger_drain_multiplier", data.hungerDrainAddition(), builder);
    }
}
