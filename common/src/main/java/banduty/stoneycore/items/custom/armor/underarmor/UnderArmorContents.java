package banduty.stoneycore.items.custom.armor.underarmor;

import banduty.stoneycore.data.SCDataComponents;
import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import banduty.stoneycore.definitions.ArmorAttachmentDefinitionsStorage;
import banduty.stoneycore.definitions.ArmorAttachmentSlotDefinitionData;
import banduty.stoneycore.definitions.ArmorAttachmentSlotDefinitionsStorage;
import banduty.stoneycore.util.SCInventoryItemFinder;
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

            if (!armorAttachment.canEquip(underArmorStack, player, incoming)) return null;

            if (!(underArmorStack.getItem() instanceof ArmorItem armorItem)) return null;
            ArmorItem.Type armorType = armorItem.getType();

            ArmorAttachmentSlotDefinitionData incomingSlotDef =
                    ArmorAttachmentSlotDefinitionsStorage.getData(incoming, armorType);

            // getData already filters by armor type, so a non-default result is valid for this piece
            if (Objects.equals(incomingSlotDef, ArmorAttachmentSlotDefinitionsStorage.getDefaultData()))
                return null;

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

            String incomingSlot = incomingSlotDef.slot();

            if (conflictsWithWornAttachments(incoming, incomingSlot, armorType, player)) return null;

            ItemStack singleItem = incoming.copyWithCount(1);

            for (int i = 0; i < this.attachments.size(); i++) {
                ItemStack existing = this.attachments.get(i);
                ArmorAttachmentSlotDefinitionData existingDef =
                        ArmorAttachmentSlotDefinitionsStorage.getData(existing, armorType);

                if (existingDef != null && Objects.equals(existingDef.slot(), incomingSlot)) {
                    this.attachments.set(i, singleItem);
                    return existing;
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
                ItemStack stack = this.attachments.get(i);
                if (stack.isEmpty()) continue;

                ArmorAttachmentSlotDefinitionData def =
                        ArmorAttachmentSlotDefinitionsStorage.getData(stack, slot);
                if (def.slot().isEmpty()) continue; // not valid for this armor type

                protectedSlots.addAll(def.protectedSlots());
                candidates.add(new AttachmentRef(i, stack, def.slot()));
            }

            List<AttachmentRef> eligible = new ArrayList<>(
                    candidates.stream().filter(r -> !protectedSlots.contains(r.slotName())).toList());
            if (eligible.isEmpty()) return false;

            Collections.shuffle(eligible);

            // Calculate total durability capacity of all eligible attachments
            int totalDurability = 0;
            for (AttachmentRef ref : eligible) {
                totalDurability += ref.stack.getMaxDamage();
            }

            // Proportional distribution based on max durability
            int remainingDamage = damageAmount;
            for (int i = 0; i < eligible.size() - 1; i++) {
                AttachmentRef ref = eligible.get(i);
                int proportionalDamage = totalDurability > 0
                        ? (int) Math.round((double) damageAmount * ref.stack.getMaxDamage() / totalDurability)
                        : 0;
                proportionalDamage = Math.min(proportionalDamage, remainingDamage);
                applyDamageToAttachment(ref, proportionalDamage, entity, slot);
                remainingDamage -= proportionalDamage;
            }

            // Apply remaining damage to the last attachment
            applyDamageToAttachment(eligible.getLast(), remainingDamage, entity, slot);

            // Remove broken attachments only after all damage is applied, so indices stay valid
            this.attachments.removeIf(ItemStack::isEmpty);

            return true;
        }

        private boolean conflictsWithWornAttachments(ItemStack incoming, String incomingSlot,
                                                     ArmorItem.Type targetType, Player player) {
            List<String> incomingIncompatible = ArmorAttachmentDefinitionsStorage.getData(incoming).incompatibleSlots();

            for (ArmorItem.Type type : ArmorItem.Type.values()) {
                if (type == ArmorItem.Type.BODY) continue;

                List<ItemStack> worn;
                if (type == targetType) {
                    worn = this.attachments;
                } else {
                    ItemStack piece = SCInventoryItemFinder.findUnderArmor(player, type);
                    if (piece.isEmpty()) continue;
                    worn = piece.getOrDefault(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY)
                            .attachments();
                }

                for (ItemStack existing : worn) {
                    String existingSlot = ArmorAttachmentSlotDefinitionsStorage.getData(existing, type).slot();
                    if (existingSlot.isEmpty()) continue;

                    // The item in the same slot is about to be replaced, so it can't conflict
                    if (type == targetType && Objects.equals(existingSlot, incomingSlot)) continue;

                    if (incomingIncompatible.contains(existingSlot)) return true;
                    if (ArmorAttachmentDefinitionsStorage.getData(existing).incompatibleSlots().contains(incomingSlot)) return true;
                }
            }
            return false;
        }

        private void applyDamageToAttachment(AttachmentRef ref, int damageAmount, LivingEntity entity, ArmorItem.Type slot) {
            if (damageAmount <= 0) return;

            ItemStack copy = ref.stack.copy();
            // Clamp damage to avoid damaging more than item can take
            int actualDamage = Math.min(damageAmount, copy.getMaxDamage());
            copy.hurtAndBreak(actualDamage, entity, slot.getSlot());

            // A broken copy is empty; it is removed by the caller once all damage is applied
            this.attachments.set(ref.index, copy);
        }

        private record AttachmentRef(int index, ItemStack stack, String slotName) {
        }
    }
}