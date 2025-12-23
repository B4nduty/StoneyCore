package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.CraftingPreviewCallback;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.IntStream;

@Mixin(CraftingMenu.class)
public abstract class CraftingScreenHandlerMixin {
    @Inject(
            method = "slotChangedCraftingGrid",
            at = @At("TAIL")
    )
    private static void afterUpdateResult(AbstractContainerMenu handler, Level level, Player player,
                                          CraftingContainer craftingInventory, ResultContainer resultInventory, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide()) return;

        ItemStack result = resultInventory.getItem(0);
        if (result.isEmpty()) return;

        ItemStack original = IntStream.range(0, craftingInventory.getContainerSize())
                .mapToObj(craftingInventory::getItem)
                .filter(stack -> !stack.isEmpty() && stack.is(result.getItem()))
                .findFirst()
                .orElse(ItemStack.EMPTY);

        ItemStack modified = CraftingPreviewCallback.EVENT.invoker()
                .modifyResult(serverPlayer, craftingInventory, original.copy());

        if (!ItemStack.matches(original, modified)) {
            resultInventory.setItem(0, modified);
            handler.setRemoteSlot(0, modified);

            serverPlayer.connection.send(
                    new ClientboundContainerSetSlotPacket(handler.containerId, handler.incrementStateId(), 0, modified)
            );
        }
    }
}