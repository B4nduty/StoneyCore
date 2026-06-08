package banduty.stoneycore.util.data.itemdata;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.platform.Services;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface SCDataComponents {
    Supplier<DataComponentType<ResourceLocation>> LOADED_ARROW = register("loaded_arrow",
            builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));

    Supplier<DataComponentType<Long>> IGNITE_TIME = register("ignite_time",
            builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    Supplier<DataComponentType<ItemStackHolder>> TARGET_STACK = register("target_stack",
            builder -> builder.persistent(ItemStackHolder.CODEC)
                    .networkSynchronized(ItemStackHolder.STREAM_CODEC));

    Supplier<DataComponentType<Boolean>> HELD_BY_TONGS_KEY = register("held_by_tongs_key",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<Boolean>> BLUDGEONING = register("bludgeoning",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<Boolean>> IGNITED = register("ignited",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<Boolean>> VISOR_OPEN = register("visor_open",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<Boolean>> RELOADING = register("is_reloading", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    Supplier<DataComponentType<Boolean>> CHARGED = register("is_charged", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    Supplier<DataComponentType<Boolean>> SHOOTING = register("is_shooting", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    Supplier<DataComponentType<UnderArmorContents>> UNDER_ARMOR_CONTENTS = register("under_armor_contents",
            builder -> builder.persistent(UnderArmorContents.CODEC).networkSynchronized(UnderArmorContents.STREAM_CODEC));

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