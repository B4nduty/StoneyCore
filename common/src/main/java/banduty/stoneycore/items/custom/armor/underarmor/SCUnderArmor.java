package banduty.stoneycore.items.custom.armor.underarmor;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SCUnderArmor extends ArmorItem {

    public SCUnderArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack underArmorStack, Slot slot,
                                          ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        UnderArmorContents.Mutable mutable = new UnderArmorContents.Mutable(
                underArmorStack.getOrDefault(
                        SCDataComponents.UNDER_ARMOR_CONTENTS.get(),
                        UnderArmorContents.EMPTY));

        ItemStack slotStack = slot.getItem();

        if (slotStack.isEmpty()) {
            // Extract
            ItemStack extracted = mutable.removeLast();
            if (extracted.isEmpty()) {
                return true;
            }

            ItemStack remaining = slot.safeInsert(extracted);

            if (!remaining.isEmpty()) {
                mutable.tryInsert(remaining, player, underArmorStack);
                return true;
            }

            saveContents(underArmorStack, mutable);
            rebuildAttachmentAttributes(underArmorStack);
            playSound(player, this.getMaterial().value().equipSound().value());
            return true;
        }

        // Insert
        ItemStack result = mutable.tryInsert(slotStack, player, underArmorStack);
        if (result == null) {
            return false;
        }

        slotStack.shrink(1);

        if (!result.isEmpty()) {
            slot.set(result);
        }

        saveContents(underArmorStack, mutable);
        rebuildAttachmentAttributes(underArmorStack);
        playSound(player, this.getMaterial().value().equipSound().value());
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack underArmorStack,
                                            ItemStack incomingStack,
                                            Slot slot,
                                            ClickAction action,
                                            Player player,
                                            SlotAccess access) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        UnderArmorContents.Mutable mutable = new UnderArmorContents.Mutable(
                underArmorStack.getOrDefault(
                        SCDataComponents.UNDER_ARMOR_CONTENTS.get(),
                        UnderArmorContents.EMPTY));

        if (incomingStack.isEmpty()) {
            // Extract onto cursor
            ItemStack extracted = mutable.removeLast();
            if (extracted.isEmpty()) {
                return true;
            }

            access.set(extracted);

            saveContents(underArmorStack, mutable);
            rebuildAttachmentAttributes(underArmorStack);
            playSound(player, this.getMaterial().value().equipSound().value());
            return true;
        }

        // Insert from cursor
        ItemStack result = mutable.tryInsert(incomingStack, player, underArmorStack);
        if (result == null) {
            return false;
        }

        incomingStack.shrink(1);

        if (!result.isEmpty()) {
            access.set(result);
        }

        saveContents(underArmorStack, mutable);
        rebuildAttachmentAttributes(underArmorStack);
        playSound(player, this.getMaterial().value().equipSound().value());
        return true;
    }

    private void saveContents(ItemStack underArmorStack,
                              UnderArmorContents.Mutable mutable) {
        UnderArmorContents newContents = mutable.toImmutable();

        if (newContents.isEmpty()) {
            underArmorStack.remove(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
        } else {
            underArmorStack.set(
                    SCDataComponents.UNDER_ARMOR_CONTENTS.get(),
                    newContents);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        UnderArmorContents contents = stack.getOrDefault(
                SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY);
        return Optional.of(new UnderArmorTooltip(contents, this.getType()));
    }

    private void playSound(Player player, SoundEvent sound) {
        player.level().playSound(
                player, player.blockPosition(),
                sound,
                SoundSource.PLAYERS,
                0.8F,
                0.8F + player.getRandom().nextFloat() * 0.4F
        );
        
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);

        UnderArmorContents contents =
                stack.get(SCDataComponents.UNDER_ARMOR_CONTENTS.get());

        if (contents != null) {
            UnderArmorContents cleaned =
                    new UnderArmorContents(
                            contents.attachments()
                                    .stream()
                                    .filter(item -> !item.isEmpty())
                                    .toList()
                    );

            if (cleaned.isEmpty()) {
                stack.remove(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
            } else {
                stack.set(
                        SCDataComponents.UNDER_ARMOR_CONTENTS.get(),
                        cleaned
                );
            }
        }

        rebuildAttachmentAttributes(stack);
    }

    public void rebuildAttachmentAttributes(ItemStack stack) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        EquipmentSlot slot = this.getType().getSlot();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);

        Map<Holder<Attribute>, Double> addValueModifiers = new HashMap<>();
        Map<Holder<Attribute>, Double> addMultipliedModifiers = new HashMap<>();

        this.getDefaultAttributeModifiers().modifiers().forEach(entry -> {
            if (entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                addValueModifiers.merge(entry.attribute(), entry.modifier().amount(), Double::sum);
            } else {
                addMultipliedModifiers.merge(entry.attribute(), entry.modifier().amount(), Double::sum);
            }
        });

        if (ArmorDefinitionsStorage.containsItem(stack)) {
            var data = ArmorDefinitionsStorage.getData(stack);
            if (data.deflectChance() != 0) {
                addValueModifiers.merge(SCAttributes.DEFLECT_CHANCE, data.deflectChance(), Double::sum);
            }
        }

        for (ItemStack armorAttachmentStack : getArmorAttachments(stack)) {
            if (armorAttachmentStack.getItem() instanceof ArmorAttachment attachment) {
                attachment.applyAttachmentAttributes(armorAttachmentStack, stack, (attribute, amount, operation) -> {
                    if (amount == 0) return;
                    if (operation == AttributeModifier.Operation.ADD_VALUE) {
                        addValueModifiers.merge(attribute, amount, Double::sum);
                    } else {
                        addMultipliedModifiers.merge(attribute, amount, Double::sum);
                    }
                });
            }
        }

        addValueModifiers.forEach((attribute, totalAmount) -> {
            if (totalAmount == 0) return;
            builder.add(attribute,
                    new AttributeModifier(createModifierId(attribute, slot.getName()),
                            totalAmount, AttributeModifier.Operation.ADD_VALUE),
                    group);
        });

        addMultipliedModifiers.forEach((attribute, totalAmount) -> {
            if (totalAmount == 0) return;
            builder.add(attribute,
                    new AttributeModifier(createModifierId(attribute, slot.getName() + "_multiplier"),
                            totalAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                    group);
        });

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private ResourceLocation createModifierId(Holder<Attribute> attribute, String suffix) {
        return attribute.unwrapKey()
                .map(k -> {
                    ResourceLocation loc = k.location();
                    String sanitizedPath = loc.getPath().replace('/', '_') + "." + suffix;
                    return loc.getNamespace().equals("minecraft")
                            ? ResourceLocation.withDefaultNamespace(sanitizedPath)
                            : ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), sanitizedPath);
                })
                .orElseGet(() ->
                        ResourceLocation.fromNamespaceAndPath(
                                StoneyCore.MOD_ID,
                                "underarmor_" + System.identityHashCode(attribute) + "_" + suffix));
    }

    public static List<ItemStack> getArmorAttachments(ItemStack stack) {
        UnderArmorContents contents =
                stack.get(SCDataComponents.UNDER_ARMOR_CONTENTS.get());

        if (contents == null || contents.isEmpty()) {
            return List.of();
        }

        return contents.attachments()
                .stream()
                .filter(item -> !item.isEmpty())
                .toList();
    }
}