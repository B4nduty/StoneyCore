package banduty.stoneycore.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record CapturedItemData(
        ResourceLocation identifier,
        int count,
        DataComponentPatch patch
) {

    public static final Codec<CapturedItemData> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("identifier")
                            .forGetter(CapturedItemData::identifier),

                    Codec.INT
                            .optionalFieldOf("count", 1)
                            .forGetter(CapturedItemData::count),

                    DataComponentPatch.CODEC
                            .optionalFieldOf(
                                    "data_components_patch",
                                    DataComponentPatch.EMPTY
                            )
                            .forGetter(CapturedItemData::patch)

            ).apply(instance, CapturedItemData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CapturedItemData> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    CapturedItemData::identifier,

                    ByteBufCodecs.INT,
                    CapturedItemData::count,

                    DataComponentPatch.STREAM_CODEC,
                    CapturedItemData::patch,

                    CapturedItemData::new
            );

    public CapturedItemData(ItemStack stack) {
        this(
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                stack.getCount(),
                stack.getComponentsPatch()
        );
    }

    public static CapturedItemData fromItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return new CapturedItemData(stack);
    }

    public ItemStack toItemStack() {
        ItemStack stack = BuiltInRegistries.ITEM.getOptional(identifier)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);

        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        stack.setCount(count);
        stack.applyComponents(patch);

        return stack;
    }

    public boolean isEmpty() {
        return toItemStack().isEmpty();
    }
}