package banduty.stoneycore.platform;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.config.ConfigImpl;
import banduty.stoneycore.config.NeoForgeConfigImpl;
import banduty.stoneycore.event.StartTickHandler;
import banduty.stoneycore.lands.util.ClaimWorker;
import banduty.stoneycore.networking.payload.LandTitleS2CPacket;
import banduty.stoneycore.platform.services.IPlatformHelper;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {
    private static final Map<ResourceKey<? extends Registry<?>>, DeferredRegister<?>> REGISTRIES = new HashMap<>();
    private final ConfigImpl config;

    public NeoForgePlatformHelper() {
        this.config = new NeoForgeConfigImpl();
    }

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }

    @Override
    public void sendTitle(ServerPlayer player, Component mainTitle) {
        PacketDistributor.sendToPlayer(player, new LandTitleS2CPacket(mainTitle));
    }

    @Override
    public Queue<ClaimWorker> getClaimTasks() {
        return StartTickHandler.CLAIM_TASKS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> register(Registry<T> registry, String name, Supplier<T> entry) {
        DeferredRegister<T> deferredRegister = (DeferredRegister<T>) REGISTRIES.computeIfAbsent(
                registry.key(),
                k -> DeferredRegister.create(registry.key(), StoneyCore.MOD_ID)
        );

        return deferredRegister.register(name, entry);
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T> Holder<T> registerHolder(ResourceKey<Registry<T>> registryKey, String name, Supplier<T> value) {
        DeferredRegister<T> deferredRegister = (DeferredRegister<T>) REGISTRIES.computeIfAbsent(
                registryKey,
                k -> DeferredRegister.create(registryKey, StoneyCore.MOD_ID)
        );

        return deferredRegister.register(name, value);
    }

    @Override
    public ConfigImpl getConfig() {
        return config;
    }

    @Override
    public List<ItemStack> getEquippedAccessories(LivingEntity livingEntity) {
        List<ItemStack> itemStacks = new ArrayList<>();

        if (!ModList.get().isLoaded("accessories")) {
            return itemStacks;
        }

        if (AccessoriesCapability.getOptionally(livingEntity).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(livingEntity).getAllEquipped()) {
                ItemStack itemStack = equipped.stack();
                if (itemStack.isEmpty()) continue;
                itemStacks.add(itemStack);
            }
        }
        return itemStacks;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(
            String name, BiFunction<BlockPos, BlockState, T> factory, Supplier<Block> blockSupplier) {

        return (Supplier<BlockEntityType<T>>) (Supplier<?>) register(BuiltInRegistries.BLOCK_ENTITY_TYPE, name,
                () -> BlockEntityType.Builder.of(factory::apply, blockSupplier.get()).build(null));
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(IFactory<T> factory) {
        return IMenuTypeExtension.create(factory::create);
    }

    public static void registerRegistries(IEventBus eventBus) {
        REGISTRIES.values().forEach(dr -> dr.register(eventBus));
    }
}