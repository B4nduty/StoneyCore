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

    default boolean canEquip(ItemStack underArmorStack, Player player, ItemStack attachmentStack) {
        return true;
    }

    default void applyAttachmentAttributes(ItemStack attachmentStack, ItemStack underArmorStack,
                                           AttributeAccumulator accumulator) {
        double baseArmor = 0;
        double baseToughness = 0;
        double baseHunger = 0;
        double baseDeflect = 0;

        if (ArmorAttachmentDefinitionsStorage.containsItem(attachmentStack)) {
            var data = ArmorAttachmentDefinitionsStorage.getData(attachmentStack);
            baseArmor += data.armor();
            baseToughness += data.toughness();
            baseHunger += data.hungerDrainMultiplier();
            baseDeflect += data.deflectChance();
        }

        if (Boolean.TRUE.equals(attachmentStack.get(SCDataComponents.VISOR_OPEN.get()))) {
            baseArmor -= 1.0;
            baseToughness -= 1.0;
            baseDeflect -= 0.05;
        }

        accumulator.accept(Attributes.ARMOR, baseArmor, AttributeModifier.Operation.ADD_VALUE);
        accumulator.accept(Attributes.ARMOR_TOUGHNESS, baseToughness, AttributeModifier.Operation.ADD_VALUE);
        accumulator.accept(SCAttributes.HUNGER_DRAIN_MULTIPLIER, baseHunger, AttributeModifier.Operation.ADD_VALUE);
        accumulator.accept(SCAttributes.DEFLECT_CHANCE, baseDeflect, AttributeModifier.Operation.ADD_VALUE);
    }

    @FunctionalInterface
    interface AttributeAccumulator {
        void accept(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation);
    }

    default InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand,
                                                   ArmorItem.Type type) {
        ItemStack stack  = player.getItemInHand(hand);
        ItemStack target = findUnderArmor(player, type);

        if (level.isClientSide) {
            return target.isEmpty()
                    ? InteractionResultHolder.pass(stack)
                    : InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (target.isEmpty() || !(target.getItem() instanceof SCUnderArmor underArmor)) {
            return InteractionResultHolder.pass(stack);
        }

        UnderArmorContents contents = target.getOrDefault(SCDataComponents.UNDER_ARMOR_CONTENTS.get(),
                UnderArmorContents.EMPTY);
        UnderArmorContents.Mutable mutable = new UnderArmorContents.Mutable(contents);

        ItemStack result = mutable.tryInsert(stack, player, target);

        if (result != null) {
            if (!result.isEmpty()) {
                player.getInventory().placeItemBackInInventory(result);
            }
            target.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), mutable.toImmutable());
            underArmor.rebuildAttachmentAttributes(target);

            level.playSound(null, player.blockPosition(),
                    underArmor.getMaterial().value().equipSound().value(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);

            stack.shrink(1);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }
}