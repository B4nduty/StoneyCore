package banduty.stoneycore.platform;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.config.ConfigImpl;
import banduty.stoneycore.config.FabricConfigImpl;
import banduty.stoneycore.event.StartTickHandler;
import banduty.stoneycore.lands.util.ClaimWorker;
import banduty.stoneycore.networking.payload.LandTitleS2CPacket;
import banduty.stoneycore.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {
    private final ConfigImpl config;

    public FabricPlatformHelper() {
        this.config = new FabricConfigImpl();
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getRecipeRemainder();
    }

    @Override
    public void sendTitle(ServerPlayer player, Component mainTitle) {
        ServerPlayNetworking.send(player, new LandTitleS2CPacket(mainTitle));
    }

    @Override
    public Queue<ClaimWorker> getClaimTasks() {
        return StartTickHandler.CLAIM_TASKS;
    }

    @Override
    public <T> Supplier<T> register(Registry<T> registry, String name, Supplier<T> entry) {
        T result = Registry.register(registry, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, name), entry.get());
        return () -> result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Holder<T> registerHolder(ResourceKey<Registry<T>> registryKey, String name, Supplier<T> value) {
        return Registry.registerForHolder(
                (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location()),
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, name),
                value.get()
        );
    }

    @Override
    public ConfigImpl getConfig() {
        return config;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(String name, BiFunction<BlockPos, BlockState, T> factory, Supplier<Block> block) {
        BlockEntityType<T> type = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, name),
                BlockEntityType.Builder.of(factory::apply, block.get()).build(null)
        );

        return () -> type;
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(IFactory<T> factory) {
        StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> passThroughCodec =
                StreamCodec.of((buf, value) -> {}, buf -> buf);

        return new ExtendedScreenHandlerType<>(factory::create, passThroughCodec);
    }
}
