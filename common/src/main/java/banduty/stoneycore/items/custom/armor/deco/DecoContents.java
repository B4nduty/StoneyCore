package banduty.stoneycore.items.custom.armor.deco;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DecoContents(List<ItemStack> items) implements TooltipComponent {
    public static final DecoContents EMPTY = new DecoContents(List.of());

    public static final Codec<DecoContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().fieldOf("items").forGetter(DecoContents::items)
    ).apply(instance, DecoContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DecoContents> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), DecoContents::items,
            DecoContents::new
    );

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int size() {
        return this.items.size();
    }

    public static class Mutable {
        private final List<ItemStack> items;

        public Mutable(DecoContents contents) {
            this.items = new ArrayList<>(contents.items());
        }

        public int tryInsert(ItemStack incoming, Item targetArmorItem) {
            if (incoming.isEmpty()) return 0;

            Optional<Deco> incomingDeco = Deco.getFromItem(incoming.getItem());
            if (incomingDeco.isEmpty()) return 0;

            if (!incomingDeco.get().canApplyTo(targetArmorItem)) return 0;

            int incomingGroup = incomingDeco.get().group();

            for (ItemStack existing : this.items) {
                Optional<Deco> existingDeco = Deco.getFromItem(existing.getItem());
                if (existingDeco.isPresent() && existingDeco.get().group() == incomingGroup) {
                    return 0;
                }
            }

            ItemStack singleItem = incoming.copyWithCount(1);
            this.items.add(singleItem);
            return 1;
        }

        public ItemStack removeLast() {
            if (this.items.isEmpty()) return ItemStack.EMPTY;
            return this.items.removeLast();
        }

        public DecoContents toImmutable() {
            return new DecoContents(List.copyOf(this.items));
        }
    }
}