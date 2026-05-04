package banduty.stoneycore.util.data.itemdata;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public interface SCDataComponents {
    DataComponentType<ResourceLocation> LOADED_ARROW = register("loaded_arrow",
            builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));

    DataComponentType<Long> IGNITE_TIME = register("ignite_time",
            builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    DataComponentType<ItemStack> TARGET_STACK = register("target_stack",
            builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC));

    DataComponentType<Boolean> HELD_BY_TONGS_KEY = register("held_by_tongs_key",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    DataComponentType<Boolean> BLUDGEONING = register("bludgeoning",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    DataComponentType<Boolean> IGNITED = register("ignited",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    DataComponentType<Boolean> VISOR_OPEN = register("visor_open",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    DataComponentType<Boolean> RELOADING = register("is_reloading", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    DataComponentType<Boolean> CHARGED = register("is_charged", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    DataComponentType<Boolean> SHOOTING = register("is_shooting", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    @SuppressWarnings("unchecked")
    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return (DataComponentType<T>) Services.PLATFORM.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                name,
                () -> builderOperator.apply(DataComponentType.builder()).build()
        ).get();
    }

    static void register() {
        StoneyCore.LOG.info("Registering Data Components for " + StoneyCore.MOD_ID);
    }
}