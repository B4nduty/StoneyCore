package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public interface CraftingPreviewCallback {
    Event<CraftingPreviewCallback> EVENT = EventFactory.createArrayBacked(
            CraftingPreviewCallback.class,
            listeners -> (player, inventory, original) -> {
                ItemStack result = original;
                for (CraftingPreviewCallback listener : listeners) {
                    result = listener.modifyResult(player, inventory, result);
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
    ItemStack modifyResult(ServerPlayerEntity player, RecipeInputInventory inventory, ItemStack original);
}