package banduty.stoneycore.items.custom.armor;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentDefinitionsStorage;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ArmorAttachment {
    default boolean hasOpenVisor(ItemStack stack) {
        return false;
    }

    @NotNull ArmorItem.Type getArmorSlot();

    default boolean canEquip(ItemStack underArmorStack, Player player) {
        return true;
    }

    default void applyAttachmentAttributes(ItemStack attachmentStack, ItemStack underArmorStack, AttributeAccumulator accumulator) {
        double baseAttachmentArmor = 0;
        double baseAttachmentToughness = 0;
        double baseAttachmentHunger = 0;
        double baseAttachmentDeflect = 0;

        if (ArmorAttachmentDefinitionsStorage.containsItem(attachmentStack)) {
            var data = ArmorAttachmentDefinitionsStorage.getData(attachmentStack);
            baseAttachmentArmor += data.armor();
            baseAttachmentToughness += data.toughness();
            baseAttachmentHunger += data.hungerDrainMultiplier();
            baseAttachmentDeflect += data.deflectChance();
        }

        if (Boolean.TRUE.equals(attachmentStack.get(SCDataComponents.VISOR_OPEN.get()))) {
            baseAttachmentArmor -= 1.0;
            baseAttachmentToughness -= 1.0;
            baseAttachmentDeflect -= 0.05;
        }

        accumulator.accept(Attributes.ARMOR, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_attachments_armor"),
                baseAttachmentArmor, AttributeModifier.Operation.ADD_VALUE));
        accumulator.accept(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_attachments_toughness"),
                baseAttachmentToughness, AttributeModifier.Operation.ADD_VALUE));
        accumulator.accept(SCAttributes.HUNGER_DRAIN_MULTIPLIER, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_attachments_hunger"),
                baseAttachmentHunger, AttributeModifier.Operation.ADD_VALUE));
        accumulator.accept(SCAttributes.DEFLECT_CHANCE, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_attachments_deflect"),
                baseAttachmentDeflect, AttributeModifier.Operation.ADD_VALUE));
    }

    @FunctionalInterface
    interface AttributeAccumulator {
        void accept(Holder<Attribute> attribute, AttributeModifier modifier);
    }
}
