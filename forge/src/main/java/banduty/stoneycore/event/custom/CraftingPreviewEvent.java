package banduty.stoneycore.event.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.common.MinecraftForge;

public class CraftingPreviewEvent extends Event {

    private final ServerPlayer player;
    private final CraftingContainer inventory;
    private ItemStack result;

    public CraftingPreviewEvent(ServerPlayer player,
                                CraftingContainer inventory,
                                ItemStack result) {
        this.player = player;
        this.inventory = inventory;
        this.result = result;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public CraftingContainer getInventory() {
        return inventory;
    }

    public ItemStack getItemResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    /**
     * Utility method to fire the event.
     */
    public static ItemStack fire(ServerPlayer player,
                                 CraftingContainer inventory,
                                 ItemStack original) {

        CraftingPreviewEvent event =
                new CraftingPreviewEvent(player, inventory, original);

        MinecraftForge.EVENT_BUS.post(event);

        return event.getItemResult();
    }
}