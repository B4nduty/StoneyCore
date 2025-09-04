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

    @Mixin(CraftingScreenHandler.class)
    public abstract class CraftingScreenHandlerMixin {
        @Inject(
                method = "updateResult",
                at = @At("TAIL")
        )
        private static void afterUpdateResult(ScreenHandler handler, World world, PlayerEntity player,
                                              RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, CallbackInfo ci) {
            if (!(player instanceof ServerPlayerEntity serverPlayerEntity)) return;

            ItemStack result = resultInventory.getStack(0);
            if (result.isEmpty()) return;

            ItemStack original = null;
            for (int i = 0; i < craftingInventory.size(); i++) {
                original = craftingInventory.getStack(i);
                if (!original.isEmpty() && original.isOf(resultInventory.getStack(0).getItem())) break;
            }

            if (original == null || original.isEmpty()) return;

            ItemStack modified = result.copy();

            if (original.hasNbt()) {
                if (modified.hasNbt()) {
                    modified.getNbt().copyFrom(original.getNbt());
                } else {
                    modified.setNbt(original.getNbt().copy());
                }
            }

            modified = CraftingPreviewCallback.EVENT.invoker().modifyResult(serverPlayerEntity, craftingInventory, modified.copy());

            resultInventory.setStack(0, modified);
            handler.setPreviousTrackedSlot(0, modified);
            serverPlayerEntity.networkHandler.sendPacket(
                    new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, modified)
            );
        }
    }