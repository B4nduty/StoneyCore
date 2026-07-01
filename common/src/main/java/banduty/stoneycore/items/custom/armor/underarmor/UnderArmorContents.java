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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public record UnderArmorContents(List<ItemStack> attachments) {

    public static final UnderArmorContents EMPTY = new UnderArmorContents(List.of());

    private static List<ItemStack> sanitize(List<ItemStack> input) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : input) {
            if (stack != null && !stack.isEmpty()) {
                out.add(stack);
            }
        }
        return out;
    }

    public static final Codec<UnderArmorContents> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf()
                            .xmap(UnderArmorContents::sanitize, l -> l)
                            .fieldOf("attachments")
                            .forGetter(UnderArmorContents::attachments)
            ).apply(instance, UnderArmorContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnderArmorContents> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()),
                    UnderArmorContents::attachments,
                    UnderArmorContents::new
            );

    public boolean isEmpty() {
        return this.attachments.isEmpty();
    }

    public List<ItemStack> getAttachments() {
        return this.attachments;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UnderArmorContents(List<ItemStack> other))) return false;
        if (this.attachments.size() != other.size()) return false;

        for (int i = 0; i < this.attachments.size(); i++) {
            if (!ItemStack.matches(this.attachments.get(i), other.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (ItemStack stack : attachments) {
            result = 31 * result + ItemStack.hashItemAndComponents(stack);
        }
        return result;
    }

    public static class Mutable {
        private final List<ItemStack> attachments;

        public Mutable(UnderArmorContents contents) {
            this.attachments = new ArrayList<>(sanitize(contents.attachments()));
        }

        public ItemStack tryInsert(ItemStack incoming, Player player, ItemStack underArmorStack) {
            if (incoming.isEmpty() || !(incoming.getItem() instanceof ArmorAttachment armorAttachment))
                return null;

            if (!armorAttachment.canEquip(underArmorStack, player)) return null;

            if (!(underArmorStack.getItem() instanceof ArmorItem armorItem)) return null;
            ArmorItem.Type armorType = armorItem.getType();

            ArmorAttachmentSlotDefinitionData incomingSlotDef =
                    ArmorAttachmentSlotDefinitionsStorage.getData(incoming, armorType);

            if (Objects.equals(incomingSlotDef, ArmorAttachmentSlotDefinitionsStorage.getDefaultData()))
                return null;

            ArmorItem.Type targetType = ArmorAttachmentSlotDefinitionsStorage.getArmorType(incomingSlotDef);
            if (armorType != targetType) return null;

            if (incomingSlotDef.requiredSlot() != null && !incomingSlotDef.requiredSlot().isEmpty()) {
                boolean hasRequired = false;
                for (ItemStack existing : this.attachments) {
                    ArmorAttachmentSlotDefinitionData existingDef =
                            ArmorAttachmentSlotDefinitionsStorage.getData(existing, armorType);
                    if (existingDef != null && Objects.equals(existingDef.slot(), incomingSlotDef.requiredSlot())) {
                        hasRequired = true;
                        break;
                    }
                }
                if (!hasRequired) return null;
            }

            ItemStack singleItem = incoming.copyWithCount(1);
            String incomingSlot = incomingSlotDef.slot();

            for (int i = 0; i < this.attachments.size(); i++) {
                ItemStack existing = this.attachments.get(i);
                ArmorAttachmentSlotDefinitionData existingDef =
                        ArmorAttachmentSlotDefinitionsStorage.getData(existing, armorType);

                if (existingDef != null && Objects.equals(existingDef.slot(), incomingSlot)) {
                    ItemStack old = existing;
                    this.attachments.set(i, singleItem);
                    return old;
                }
            }

            this.attachments.add(singleItem);
            return ItemStack.EMPTY;
        }

        public ItemStack removeLast() {
            if (this.attachments.isEmpty()) return ItemStack.EMPTY;
            return this.attachments.removeLast();
        }

        public UnderArmorContents toImmutable() {
            return new UnderArmorContents(List.copyOf(sanitize(this.attachments)));
        }

        public boolean damageAttachment(ArmorItem.Type slot, int damageAmount, LivingEntity entity) {
            if (this.attachments.isEmpty()) return false;

            Set<String> protectedSlots = new HashSet<>();
            List<AttachmentRef> candidates = new ArrayList<>();

            for (int i = 0; i < this.attachments.size(); i++) {
                ItemStack attachmentStack = this.attachments.get(i);
                if (attachmentStack.isEmpty()) continue;

                if (ArmorAttachmentDefinitionsStorage.containsItem(attachmentStack)) {
                    String slotFromJson = ArmorAttachmentDefinitionsStorage.getData(attachmentStack.getItem()).armorSlot();

                    if (!slotFromJson.isBlank() && slotFromJson.equalsIgnoreCase(slot.getName())) {
                        ArmorAttachmentSlotDefinitionData slotDef = ArmorAttachmentSlotDefinitionsStorage.getData(attachmentStack, slot);

                        protectedSlots.addAll(slotDef.protectedSlots());
                        candidates.add(new AttachmentRef(i, attachmentStack, slotDef.slot()));
                    }
                }
            }

            boolean anyDamageApplied = false;

            for (int i = candidates.size() - 1; i >= 0; i--) {
                AttachmentRef candidate = candidates.get(i);

                if (protectedSlots.contains(candidate.slotName)) continue;

                ItemStack copy = candidate.stack.copy();
                copy.hurtAndBreak(damageAmount, entity, slot.getSlot());

                if (copy.isEmpty()) {
                    this.attachments.remove(candidate.index);
                } else {
                    this.attachments.set(candidate.index, copy);
                }

                anyDamageApplied = true;
            }

            return anyDamageApplied;
        }

        private record AttachmentRef(int index, ItemStack stack, String slotName) {}
    }
}