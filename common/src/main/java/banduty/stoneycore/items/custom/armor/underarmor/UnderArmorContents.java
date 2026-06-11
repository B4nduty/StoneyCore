package banduty.stoneycore.items.custom.armor.underarmor;

import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentSlotDefinitionData;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentSlotDefinitionsStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record UnderArmorContents(List<ItemStack> attachments) {
    public static final UnderArmorContents EMPTY = new UnderArmorContents(List.of());

    public static final Codec<UnderArmorContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().fieldOf("attachments").forGetter(UnderArmorContents::attachments)
    ).apply(instance, UnderArmorContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnderArmorContents> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), UnderArmorContents::attachments,
            UnderArmorContents::new
    );

    public boolean isEmpty() {
        return this.attachments.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UnderArmorContents(List<ItemStack> attachments1))) return false;
        if (this.attachments.size() != attachments1.size()) return false;

        for (int i = 0; i < this.attachments.size(); i++) {
            if (!ItemStack.matches(this.attachments.get(i), attachments1.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (ItemStack stack : attachments) {
            result = 31 * result + (stack.isEmpty() ? 0 : stack.getItem().hashCode());
        }
        return result;
    }

    public static class Mutable {
        private final List<ItemStack> attachments;

        public Mutable(UnderArmorContents contents) {
            this.attachments = new ArrayList<>(contents.attachments());
        }

        public ItemStack tryInsert(ItemStack incoming, Player player, ItemStack underArmorStack) {
            if (incoming.isEmpty() || !(incoming.getItem() instanceof ArmorAttachment armorAttachment))
                return null;

            if (!armorAttachment.canEquip(underArmorStack, player)) return null;

            ArmorItem.Type armorType;
            if (underArmorStack.getItem() instanceof ArmorItem armorItem) {
                armorType = armorItem.getType();
            } else {
                return null;
            }

            ArmorAttachmentSlotDefinitionData incomingSlotDef =
                    ArmorAttachmentSlotDefinitionsStorage.getData(incoming, armorType);

            if (Objects.equals(incomingSlotDef, ArmorAttachmentSlotDefinitionsStorage.getDefaultData())) {
                return null;
            }

            ArmorItem.Type targetType = ArmorAttachmentSlotDefinitionsStorage.getArmorType(incomingSlotDef);

            if (armorItem.getType() != targetType) return null;

            if (incomingSlotDef.requiredSlot() != null && !incomingSlotDef.requiredSlot().isEmpty()) {
                boolean hasRequiredAttachment = false;

                for (ItemStack existing : this.attachments) {
                    ArmorAttachmentSlotDefinitionData existingDef =
                            ArmorAttachmentSlotDefinitionsStorage.getData(existing, armorType);

                    if (existingDef != null &&
                            Objects.equals(existingDef.slot(), incomingSlotDef.requiredSlot())) {
                        hasRequiredAttachment = true;
                        break;
                    }
                }

                if (!hasRequiredAttachment) return null;
            }

            ItemStack singleItem = incoming.copyWithCount(1);
            String incomingSlot = incomingSlotDef.slot();

            for (int i = 0; i < this.attachments.size(); i++) {
                ItemStack existing = this.attachments.get(i);

                ArmorAttachmentSlotDefinitionData existingDef =
                        ArmorAttachmentSlotDefinitionsStorage.getData(existing, armorType);

                if (existingDef != null &&
                        Objects.equals(existingDef.slot(), incomingSlot)) {

                    ItemStack old = existing;

                    // swap
                    this.attachments.set(i, singleItem);

                    // return replaced item
                    return old;
                }
            }

            // no slot match → normal insert
            this.attachments.add(singleItem);
            return ItemStack.EMPTY;
        }

        public ItemStack removeLast() {
            if (this.attachments.isEmpty()) return ItemStack.EMPTY;
            return this.attachments.removeLast();
        }

        public UnderArmorContents toImmutable() {
            return new UnderArmorContents(List.copyOf(this.attachments));
        }

        public boolean damageAttachment(String armorSlotName, int damageAmount, LivingEntity entity, EquipmentSlot slot) {
            boolean anyDamageApplied = false;

            for (int i = 0; i < this.attachments.size(); i++) {
                ItemStack attachmentStack = this.attachments.get(i);

                if (!attachmentStack.isEmpty() && ArmorAttachmentDefinitionsStorage.containsItem(attachmentStack)) {
                    String slotFromJson = ArmorAttachmentDefinitionsStorage.getData(attachmentStack.getItem()).armorSlot();

                    if (!slotFromJson.isBlank() && slotFromJson.equalsIgnoreCase(armorSlotName)) {
                        ItemStack modifiableCopy = attachmentStack.copy();

                        modifiableCopy.hurtAndBreak(damageAmount, entity, slot);

                        this.attachments.set(i, modifiableCopy);
                        anyDamageApplied = true;
                    }
                }
            }

            return anyDamageApplied;
        }
    }
}