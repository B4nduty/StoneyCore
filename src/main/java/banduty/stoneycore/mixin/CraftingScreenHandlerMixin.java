package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.CraftingPreviewCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.IntStream;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {
    @Inject(
            method = "updateResult",
            at = @At("TAIL")
    )
    private static void afterUpdateResult(ScreenHandler handler, World world, PlayerEntity player,
                                          RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayerEntity) || world.isClient()) return;

        ItemStack result = resultInventory.getStack(0);
        if (result.isEmpty()) return;

        ItemStack original = IntStream.range(0, craftingInventory.size())
                .mapToObj(craftingInventory::getStack)
                .filter(stack -> !stack.isEmpty() && stack.isOf(result.getItem()))
                .findFirst()
                .orElse(ItemStack.EMPTY);

        ItemStack modified = CraftingPreviewCallback.EVENT.invoker()
                .modifyResult(serverPlayerEntity, craftingInventory, original.copy());

        if (!ItemStack.areEqual(original, modified)) {
            resultInventory.setStack(0, modified);
            handler.setPreviousTrackedSlot(0, modified);

            serverPlayerEntity.networkHandler.sendPacket(
                    new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, modified)
            );
        }
    }
}