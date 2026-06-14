package banduty.stoneycore.items.custom.armor;

import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentDefinitionsStorage;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static banduty.stoneycore.util.SCInventoryItemFinder.findUnderArmor;

public interface ArmorAttachment {
    default boolean hasOpenVisor(ItemStack stack) {
        return false;
    }

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

        accumulator.accept(Attributes.ARMOR, baseAttachmentArmor, AttributeModifier.Operation.ADD_VALUE);
        accumulator.accept(Attributes.ARMOR_TOUGHNESS, baseAttachmentToughness, AttributeModifier.Operation.ADD_VALUE);
        accumulator.accept(SCAttributes.HUNGER_DRAIN_MULTIPLIER, baseAttachmentHunger, AttributeModifier.Operation.ADD_VALUE);
        accumulator.accept(SCAttributes.DEFLECT_CHANCE, baseAttachmentDeflect, AttributeModifier.Operation.ADD_VALUE);
    }

    @FunctionalInterface
    interface AttributeAccumulator {
        void accept(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation);
    }

    default InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand, ArmorItem.Type type) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            ItemStack target = findUnderArmor(player, type);
            if (!target.isEmpty()) {
                return InteractionResultHolder.sidedSuccess(stack, true);
            }
            return InteractionResultHolder.pass(stack);
        }

        ItemStack target = findUnderArmor(player, type);

        if (!target.isEmpty() && target.getItem() instanceof SCUnderArmor underArmor) {

            UnderArmorContents contents =
                    target.getOrDefault(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY);

            UnderArmorContents.Mutable mutable = new UnderArmorContents.Mutable(contents);

            ItemStack result = mutable.tryInsert(stack, player, target);

            if (!result.isEmpty()) {
                player.getInventory().placeItemBackInInventory(result);
            }

            target.set(
                    SCDataComponents.UNDER_ARMOR_CONTENTS.get(),
                    mutable.toImmutable()
            );

            underArmor.rebuildAttachmentAttributes(target);

            level.playSound(
                    null,
                    player.blockPosition(),
                    underArmor.getMaterial().value().equipSound().value(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            stack.shrink(1);

            return InteractionResultHolder.success(stack);

        }

        return InteractionResultHolder.pass(stack);
    }
}
