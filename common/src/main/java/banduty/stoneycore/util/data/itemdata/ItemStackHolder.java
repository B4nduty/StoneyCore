package banduty.stoneycore.util.data.itemdata;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ItemStackHolder(ItemStack stack) {
    public static final Codec<ItemStackHolder> CODEC = ItemStack.CODEC
            .xmap(ItemStackHolder::new, ItemStackHolder::stack);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackHolder> STREAM_CODEC = ItemStack.STREAM_CODEC
            .map(ItemStackHolder::new, ItemStackHolder::stack);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStackHolder that = (ItemStackHolder) o;
        return ItemStack.matches(this.stack, that.stack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }
}