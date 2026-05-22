package banduty.stoneycore.items.custom.armor.underarmor;

import banduty.stoneycore.items.custom.armor.SCAccessory;
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

public record UnderArmorContents(List<ItemStack> accessories) {
    public static final UnderArmorContents EMPTY = new UnderArmorContents(List.of());

    public static final Codec<UnderArmorContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().fieldOf("accessories").forGetter(UnderArmorContents::accessories)
    ).apply(instance, UnderArmorContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnderArmorContents> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), UnderArmorContents::accessories,
            UnderArmorContents::new
    );

    public boolean isEmpty() {
        return this.accessories.isEmpty();
    }

    public static class Mutable {
        private final List<ItemStack> accessories;

        public Mutable(UnderArmorContents contents) {
            this.accessories = new ArrayList<>(contents.accessories());
        }

        public int tryInsert(ItemStack incoming, Player player, ItemStack underArmorStack) {
            if (incoming.isEmpty() || !(incoming.getItem() instanceof SCAccessory scAccessory)) return 0;

            if (((ArmorItem) underArmorStack.getItem()).getType() != scAccessory.getArmorSlot()) return 0;
            if (!scAccessory.canEquip(underArmorStack, player)) return 0;

            for (ItemStack existing : this.accessories) {
                if (existing.getItem() == incoming.getItem()) return 0;
                if (((SCAccessory) existing.getItem()).numberSlot() == scAccessory.numberSlot()) return 0;
            }

            ItemStack singleItem = incoming.copyWithCount(1);
            this.accessories.add(singleItem);
            return 1;
        }

        public ItemStack removeLast() {
            if (this.accessories.isEmpty()) return ItemStack.EMPTY;
            return this.accessories.removeLast();
        }

        public UnderArmorContents toImmutable() {
            return new UnderArmorContents(List.copyOf(this.accessories));
        }
    }
}