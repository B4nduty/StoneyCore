package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public interface CraftingPreviewCallback {
    Event<CraftingPreviewCallback> EVENT = EventFactory.createArrayBacked(
            CraftingPreviewCallback.class,
            listeners -> (player, inventory, original) -> {
                ItemStack result = original;
                for (CraftingPreviewCallback listener : listeners) {
                    ItemStack newResult = listener.modifyResult(player, inventory, result);
                    if (newResult != null) result = newResult;
                }
                return result;
            }
    );

    /**
     * @param player    the player crafting
     * @param inventory the crafting grid
     * @param original  the current recipe output
     * @return a possibly modified stack (can be same as original)
     */
    ItemStack modifyResult(ServerPlayer player, CraftingContainer inventory, ItemStack original);
}