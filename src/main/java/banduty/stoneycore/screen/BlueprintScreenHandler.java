
package banduty.stoneycore.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlueprintScreenHandler extends AbstractContainerMenu {
    private final ItemStack itemStack;
    private final ResourceLocation structureId;

    public BlueprintScreenHandler(int syncId, Inventory inventory, FriendlyByteBuf buf) {
        this(syncId, inventory, buf.readItem(), buf.readResourceLocation());
    }

    public BlueprintScreenHandler(int syncId, Inventory inventory, ItemStack itemStack, ResourceLocation structureId) {
        super(ModScreenHandlers.BLUEPRINT_SCREEN_HANDLER, syncId);
        this.itemStack = itemStack;
        this.structureId = structureId;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ResourceLocation getStructureId() {
        return structureId;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slot) {
        return null;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
