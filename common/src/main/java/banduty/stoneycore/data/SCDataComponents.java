package banduty.stoneycore.data;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.platform.Services;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface SCDataComponents {
    Supplier<DataComponentType<ResourceLocation>> LOADED_ARROW = register("loaded_arrow",
            builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));

    Supplier<DataComponentType<Long>> IGNITE_TIME = register("ignite_time",
            builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    Supplier<DataComponentType<Boolean>> IGNITED = register("ignited",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<String>> MANUSCRIPT_TYPE = register("manuscript_type",
            builder -> builder.persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    Supplier<DataComponentType<Boolean>> BLUDGEONING = register("bludgeoning",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<Boolean>> ARROW_IGNITED = register("arrow_ignited",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<Boolean>> VISOR_OPEN = register("visor_open",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<Boolean>> RELOADING = register("is_reloading", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    Supplier<DataComponentType<Boolean>> CHARGED = register("is_charged", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    Supplier<DataComponentType<Boolean>> SHOOTING = register("is_shooting", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<UnderArmorContents>> UNDER_ARMOR_CONTENTS = register("under_armor_contents",
            builder -> builder.persistent(UnderArmorContents.CODEC).networkSynchronized(UnderArmorContents.STREAM_CODEC));

    Supplier<DataComponentType<ItemStack>> CAPTURED_ITEM = register("captured_item",
            builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC));

    @SuppressWarnings("unchecked")
    private static <T> Supplier<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Services.PLATFORM.register(
                (Registry<DataComponentType<T>>) (Registry<?>) BuiltInRegistries.DATA_COMPONENT_TYPE,
                name,
                () -> builderOperator.apply(DataComponentType.builder()).build()
        );
    }

    static void register() {
        StoneyCore.LOG.info("Registering Data Components for " + StoneyCore.MOD_ID);
    }
}