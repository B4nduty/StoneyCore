package banduty.stoneycore.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record StackIngredient(ItemStack stack, Optional<TagKey<Item>> tag) {

    public static final MapCodec<StackIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(StackIngredient::stack),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(StackIngredient::tag)
    ).apply(inst, StackIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TagKey<Item>> TAG_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
                    .map(location -> TagKey.create(Registries.ITEM, location),
                            TagKey::location
                    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StackIngredient> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, StackIngredient::stack,
            ByteBufCodecs.optional(TAG_STREAM_CODEC), StackIngredient::tag,
            StackIngredient::new
    );

    public boolean test(ItemStack input) {
        if (tag.isPresent()) {
            return !input.isEmpty() && input.is(tag.get());
        }
        if (input.isEmpty()) return false;

        return ItemStack.isSameItemSameComponents(stack, input);
    }

    public List<ItemStack> asItemStacks() {
        if (tag.isPresent()) {
            return BuiltInRegistries.ITEM.getOrCreateTag(tag.get()).stream()
                    .map(holder -> {
                        ItemStack s = new ItemStack(holder.value());
                        s.setCount(stack.getCount());
                        return s;
                    })
                    .collect(Collectors.toList());
        } else if (!stack.isEmpty()) {
            return Collections.singletonList(stack);
        }
        return Collections.emptyList();
    }
}