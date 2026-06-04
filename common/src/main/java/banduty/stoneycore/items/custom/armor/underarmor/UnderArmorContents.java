package banduty.stoneycore.items.custom.armor.underarmor;

import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentSlotDefinitionData;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentSlotDefinitionsStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

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

    public static class Mutable {
        private final List<ItemStack> attachments;

        public Mutable(UnderArmorContents contents) {
            this.attachments = new ArrayList<>(contents.attachments());
        }

        public int tryInsert(ItemStack incoming, Player player, ItemStack underArmorStack) {
            if (incoming.isEmpty() || !(incoming.getItem() instanceof ArmorAttachment armorAttachment)) return 0;

            if (((ArmorItem) underArmorStack.getItem()).getType() != armorAttachment.getArmorSlot()) return 0;
            if (!armorAttachment.canEquip(underArmorStack, player)) return 0;

            ArmorAttachmentSlotDefinitionData incomingSlotDef = ArmorAttachmentSlotDefinitionsStorage.getData(incoming);

            if (!incomingSlotDef.requiredSlot().isEmpty()) {
                boolean hasRequiredAttachment = false;
                for (ItemStack existing : this.attachments) {
                    ArmorAttachmentSlotDefinitionData existingDef = ArmorAttachmentSlotDefinitionsStorage.getData(existing);
                    if (existingDef.slot().equals(incomingSlotDef.requiredSlot())) {
                        hasRequiredAttachment = true;
                        break;
                    }
                }
                if (!hasRequiredAttachment) return 0; // The slot is locked!
            }

            for (ItemStack existing : this.attachments) {
                if (existing.getItem() == incoming.getItem()) return 0;
                if (ArmorAttachmentSlotDefinitionsStorage.shareSameSlot(existing, incoming)) {
                    return 0;
                }
            }

            ItemStack singleItem = incoming.copyWithCount(1);
            this.attachments.add(singleItem);
            return 1;
        }

        public ItemStack removeLast() {
            if (this.attachments.isEmpty()) return ItemStack.EMPTY;
            return this.attachments.removeLast();
        }

        public UnderArmorContents toImmutable() {
            return new UnderArmorContents(List.copyOf(this.attachments));
        }
    }
}