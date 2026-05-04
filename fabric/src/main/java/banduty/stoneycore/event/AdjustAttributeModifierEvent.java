package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.core.Holder;
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

    private static void updatePlayerAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        double armor = 0;
        double toughness = 0;
        double hungerDrainMultiplier = 0;
        double deflectChance = 0;

        if (AccessoriesDefinitionsStorage.containsItem(stack)) {
            var data = AccessoriesDefinitionsStorage.getData(stack);
            armor = data.armor();
            toughness = data.toughness();
            hungerDrainMultiplier = data.hungerDrainMultiplier();
            deflectChance = data.deflectChance();
        }

        if (ArmorDefinitionsStorage.containsItem(stack)) {
            var data = ArmorDefinitionsStorage.getData(stack);
            deflectChance = data.deflectChance();
        }

        if (Boolean.TRUE.equals(stack.get(SCDataComponents.VISOR_OPEN))) {
            armor -= 1;
            toughness -= 1;
            deflectChance -= 0.05;
        }

        handleAttribute(reference, SCAttributes.HUNGER_DRAIN_MULTIPLIER, "hunger_drain_multiplier", hungerDrainMultiplier, builder);
        handleAttribute(reference, SCAttributes.DEFLECT_CHANCE, "deflect_chance", deflectChance, builder);
        handleAttribute(reference, Attributes.ARMOR, "armor", armor, builder);
        handleAttribute(reference, Attributes.ARMOR_TOUGHNESS, "armor_toughness", toughness, builder);
    }

    private static void handleAttribute(SlotReference reference, Holder<Attribute> attribute, String name, double value, AccessoryAttributeBuilder builder) {
        AttributeInstance instance = reference.entity().getAttribute(attribute);
        if (instance == null || value == 0) return;

        builder.addStackable(attribute, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, name),
                value, AttributeModifier.Operation.ADD_VALUE));
    }
}