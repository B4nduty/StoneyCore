package banduty.stoneycore.recipes;

import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import com.mojang.serialization.Codec;
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

public record StackIngredient(ItemStack stack, Optional<TagKey<Item>> tag, FinishedRequirement finishedRequirement) {

    public StackIngredient(ItemStack stack, Optional<TagKey<Item>> tag) {
        this(stack, tag, FinishedRequirement.ANY);
    }

    public enum FinishedRequirement {
        ANY,
        FINISHED,
        NOT_FINISHED;

        public static final Codec<FinishedRequirement> CODEC = Codec.STRING.xmap(
                s -> switch (s) {
                    case "finished" -> FINISHED;
                    case "not_finised" -> NOT_FINISHED;
                    default -> ANY;
                },
                requirement -> switch (requirement) {
                    case FINISHED -> "finished";
                    case NOT_FINISHED -> "not_finised";
                    case ANY -> "any";
                }
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, FinishedRequirement> STREAM_CODEC =
                ByteBufCodecs.idMapper(i -> FinishedRequirement.values()[i], FinishedRequirement::ordinal).cast();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, TagKey<Item>> TAG_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
                    .map(location -> TagKey.create(Registries.ITEM, location),
                            TagKey::location
                    );

    public static final MapCodec<StackIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("stack").forGetter(StackIngredient::stack),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(StackIngredient::tag),
            FinishedRequirement.CODEC.optionalFieldOf("finished_requirement", FinishedRequirement.ANY).forGetter(StackIngredient::finishedRequirement)
    ).apply(inst, StackIngredient::new));


    public static final StreamCodec<RegistryFriendlyByteBuf, StackIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_STREAM_CODEC,
                    StackIngredient::stack,
                    ByteBufCodecs.optional(TAG_STREAM_CODEC),
                    StackIngredient::tag,
                    FinishedRequirement.STREAM_CODEC,
                    StackIngredient::finishedRequirement,
                    StackIngredient::new
            );


    public boolean test(ItemStack input) {
        if (input.isEmpty()) return false;

        boolean isFinished = input.getOrDefault(SCDataComponents.FINISHED.get(), false);

        if (finishedRequirement == FinishedRequirement.FINISHED && !isFinished) {
            return false;
        }

        if (finishedRequirement == FinishedRequirement.NOT_FINISHED && isFinished) {
            return false;
        }

        return tag.map(input::is).orElseGet(() ->
                areItemsEqualIgnoringDataComponents(stack, input)
        );
    }

    private boolean areItemsEqualIgnoringDataComponents(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (!stack1.is(stack2.getItem())) return false;
        if (stack1.getItem() != stack2.getItem()) return false;

        var patched1 = stack1.copy();
        var patched2 = stack2.copy();
        patched1.remove(SCDataComponents.IGNITE_TIME.get());
        patched2.remove(SCDataComponents.IGNITE_TIME.get());
        patched1.remove(SCDataComponents.FINISHED.get());
        patched2.remove(SCDataComponents.FINISHED.get());

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