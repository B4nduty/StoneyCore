package banduty.stoneycore.items.custom.armor.underarmor;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
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

public class SCUnderArmor extends ArmorItem {
    public SCUnderArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack underArmorStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;

        ItemStack incomingStack = slot.getItem();
        UnderArmorContents contents = underArmorStack.getOrDefault(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY);
        UnderArmorContents.Mutable mutable = new UnderArmorContents.Mutable(contents);

        if (incomingStack.isEmpty()) {
            ItemStack extracted = mutable.removeLast();
            if (!extracted.isEmpty()) {
                underArmorStack.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), mutable.toImmutable());
                rebuildAccessoryAttributes(underArmorStack);
                slot.set(extracted);
                playRemoveSound(player);
                return true;
            }
        } else {
            int inserted = mutable.tryInsert(incomingStack, player, underArmorStack);
            if (inserted > 0) {
                incomingStack.shrink(inserted);
                underArmorStack.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), mutable.toImmutable());
                rebuildAccessoryAttributes(underArmorStack);
                playInsertSound(player);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack underArmorStack, ItemStack incomingStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) return false;

        UnderArmorContents contents = underArmorStack.getOrDefault(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY);
        UnderArmorContents.Mutable mutable = new UnderArmorContents.Mutable(contents);

        if (incomingStack.isEmpty()) {
            ItemStack extracted = mutable.removeLast();
            if (!extracted.isEmpty()) {
                underArmorStack.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), mutable.toImmutable());
                rebuildAccessoryAttributes(underArmorStack);
                access.set(extracted);
                playRemoveSound(player);
                return true;
            }
        } else {
            int inserted = mutable.tryInsert(incomingStack, player, underArmorStack);
            if (inserted > 0) {
                incomingStack.shrink(inserted);
                underArmorStack.set(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), mutable.toImmutable());
                rebuildAccessoryAttributes(underArmorStack);
                playInsertSound(player);
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        UnderArmorContents contents = stack.get(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
        if (contents == null || contents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new UnderArmorTooltip(contents));
    }

    private void playInsertSound(Player player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F, 0.8F + player.getRandom().nextFloat() * 0.4F);
    }

    private void playRemoveSound(Player player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 0.8F, 0.8F + player.getRandom().nextFloat() * 0.4F);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        rebuildAccessoryAttributes(stack);
    }

    public void rebuildAccessoryAttributes(ItemStack stack) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        ItemAttributeModifiers staticModifiers = this.getDefaultAttributeModifiers();
        staticModifiers.modifiers().forEach(entry -> {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        });

        double armor = 0;
        double toughness = 0;
        double hungerDrainMultiplier = 0;
        double deflectChance = 0;

        for (ItemStack accessoryStack : getAccessories(stack)) {
            if (AccessoriesDefinitionsStorage.containsItem(accessoryStack)) {
                var data = AccessoriesDefinitionsStorage.getData(accessoryStack);
                armor += data.armor();
                toughness += data.toughness();
                hungerDrainMultiplier += data.hungerDrainMultiplier();
                deflectChance += data.deflectChance();
            }
        }

        if (ArmorDefinitionsStorage.containsItem(stack)) {
            var data = ArmorDefinitionsStorage.getData(stack);
            deflectChance += data.deflectChance();
        }

        if (Boolean.TRUE.equals(stack.get(SCDataComponents.VISOR_OPEN.get()))) {
            armor -= 1.0;
            toughness -= 1.0;
            deflectChance -= 0.05;
        }

        EquipmentSlot slot = this.getType().getSlot();

        if (armor != 0) {
            builder.add(Attributes.ARMOR, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_accessory_armor"),
                    armor, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));
        }
        if (toughness != 0) {
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_accessory_toughness"),
                    toughness, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));
        }
        if (hungerDrainMultiplier != 0) {
            builder.add(SCAttributes.HUNGER_DRAIN_MULTIPLIER, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_accessory_hunger"),
                    hungerDrainMultiplier, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));
        }
        if (deflectChance != 0) {
            builder.add(SCAttributes.DEFLECT_CHANCE, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "underarmor_accessory_deflect"),
                    deflectChance, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    public static List<ItemStack> getAccessories(ItemStack stack) {
        UnderArmorContents contents = stack.get(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
        return contents != null ? contents.accessories() : List.of();
    }
}