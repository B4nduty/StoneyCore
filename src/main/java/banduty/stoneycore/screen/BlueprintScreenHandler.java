
package banduty.stoneycore.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class BlueprintScreenHandler extends ScreenHandler {
    private final ItemStack itemStack;
    private final Identifier structureId;

    public BlueprintScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf.readItemStack(), buf.readIdentifier());
    }

    public BlueprintScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack itemStack, Identifier structureId) {
        super(ModScreenHandlers.BLUEPRINT_SCREEN_HANDLER, syncId);
        this.itemStack = itemStack;
        this.structureId = structureId;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Identifier getStructureId() {
        return structureId;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
