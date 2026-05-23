package banduty.stoneycore.items.custom.armor.underarmor;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SCUnderArmor extends ArmorItem {

    public SCUnderArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack underArmorStack, Slot slot, ClickAction action, Player player) {
        return handleStackInteraction(underArmorStack, action, player, slot::getItem, slot::set);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack underArmorStack, ItemStack incomingStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return handleStackInteraction(underArmorStack, action, player, () -> incomingStack, access::set);
    }

    private boolean handleStackInteraction(ItemStack underArmorStack, ClickAction action, Player player,
                                           Supplier<ItemStack> incomingSupplier, Consumer<ItemStack> outputCons) {
        if (action != ClickAction.SECONDARY) return false;

        ItemStack incomingStack = incomingSupplier.get();
        UnderArmorContents contents = underArmorStack.getOrDefault(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY);
        UnderArmorContents.Mutable mutable = new UnderArmorContents.Mutable(contents);

        if (incomingStack.isEmpty()) {
            ItemStack extracted = mutable.removeLast();
            if (!extracted.isEmpty()) {
                underArmorStack.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), mutable.toImmutable());
                rebuildAttachmentAttributes(underArmorStack);
                outputCons.accept(extracted);
                playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
                return true;
            }
        } else {
            int inserted = mutable.tryInsert(incomingStack, player, underArmorStack);
            if (inserted > 0) {
                incomingStack.shrink(inserted);
                underArmorStack.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), mutable.toImmutable());
                rebuildAttachmentAttributes(underArmorStack);
                playSound(player, SoundEvents.BUNDLE_INSERT);
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        UnderArmorContents contents = stack.get(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
        return (contents == null || contents.isEmpty()) ? Optional.empty() : Optional.of(new UnderArmorTooltip(contents));
    }

    private void playSound(Player player, net.minecraft.sounds.SoundEvent sound) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 0.8F, 0.8F + player.getRandom().nextFloat() * 0.4F);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        rebuildAttachmentAttributes(stack);
    }

    public void rebuildAttachmentAttributes(ItemStack stack) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        EquipmentSlot slot = this.getType().getSlot();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);

        for (ItemStack armorAttachmentStack : getArmorAttachments(stack)) {
            if (armorAttachmentStack.getItem() instanceof ArmorAttachment attachment) {
                attachment.applyAttachmentAttributes(armorAttachmentStack, stack, (attribute, modifier) -> {
                    builder.add(attribute, modifier, group);
                });
            }
        }

        double baseDeflect = 0;
        if (ArmorDefinitionsStorage.containsItem(stack)) {
            var data = ArmorDefinitionsStorage.getData(stack);
            baseDeflect += data.deflectChance();
        }

        if (baseDeflect != 0)
            builder.add(SCAttributes.DEFLECT_CHANCE, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_deflect"),
                    baseDeflect, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    public static List<ItemStack> getArmorAttachments(ItemStack stack) {
        UnderArmorContents contents = stack.get(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
        return contents != null ? contents.attachments() : List.of();
    }
}