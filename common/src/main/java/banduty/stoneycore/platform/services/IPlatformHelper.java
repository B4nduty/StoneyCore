package banduty.stoneycore.platform.services;

import banduty.stoneycore.config.ConfigImpl;
import banduty.stoneycore.lands.util.ClaimWorker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    ItemStack getCraftingRemainingItem(ItemStack stack);

    void sendTitle(ServerPlayer player, Component mainTitle);

    Queue<ClaimWorker> getClaimTasks();

    <T> Supplier<T> register(Registry<T> registry, String name, Supplier<T> entry);

    <T> Holder<T> registerHolder(ResourceKey<Registry<T>> registryKey, String name, java.util.function.Supplier<T> value);

    ConfigImpl getConfig();

    List<ItemStack> getEquippedAccessories(LivingEntity livingEntity);

    <T extends BlockEntity> BlockEntityType<T> registerBlockEntityType(
            String name,
            BiFunction<BlockPos, BlockState, T> factory,
            Block block
    );

    <T extends AbstractContainerMenu> MenuType<T> createMenuType(IFactory<T> factory);

    interface IFactory<T extends AbstractContainerMenu> {
        T create(int syncId, Inventory inventory, RegistryFriendlyByteBuf buf);
    }
}