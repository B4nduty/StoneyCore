package banduty.stoneycore.recipes;

import banduty.stoneycore.util.data.itemdata.SCDataComponents;
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

    public static final StreamCodec<RegistryFriendlyByteBuf, TagKey<Item>> TAG_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
                    .map(location -> TagKey.create(Registries.ITEM, location),
                            TagKey::location
                    );

    public static final MapCodec<StackIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("stack").forGetter(StackIngredient::stack),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(StackIngredient::tag)
    ).apply(inst, StackIngredient::new));


    public static final StreamCodec<RegistryFriendlyByteBuf, StackIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_STREAM_CODEC,
                    StackIngredient::stack,
                    ByteBufCodecs.optional(TAG_STREAM_CODEC),
                    StackIngredient::tag,
                    StackIngredient::new
            );


    public boolean test(ItemStack input) {
        return tag.map(itemTagKey ->
                !input.isEmpty() && input.is(itemTagKey)
        ).orElseGet(() ->
                !input.isEmpty() && areItemsEqualIgnoringIgniteTime(stack, input)
        );
    }

    private boolean areItemsEqualIgnoringIgniteTime(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;

        // First check if they're the same item
        if (!stack1.is(stack2.getItem())) return false;

        // Check if they're the same item type
        if (stack1.getItem() != stack2.getItem()) return false;

        // Remove ignite time from both for comparison
        var patched1 = stack1.copy();
        var patched2 = stack2.copy();
        patched1.remove(SCDataComponents.IGNITE_TIME.get());
        patched2.remove(SCDataComponents.IGNITE_TIME.get());

        // Compare everything except the removed component
        return ItemStack.isSameItemSameComponents(patched1, patched2);
    }


    public List<ItemStack> asItemStacks() {
        if (tag.isPresent()) {
            return BuiltInRegistries.ITEM.getOrCreateTag(tag.get()).stream()
                    .map(holder -> new ItemStack(holder.value()))
                    .collect(Collectors.toList());
        }

        if (!stack.isEmpty()) {
            return Collections.singletonList(stack.copy());
        }

        return Collections.emptyList();
    }
}