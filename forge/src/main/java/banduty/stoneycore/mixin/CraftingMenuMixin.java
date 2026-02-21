package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.CraftingPreviewEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @Shadow
    private CraftingContainer craftSlots;
    @Shadow
    private ResultContainer resultSlots;
    @Shadow
    private Player player;

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void stoneycore$previewEvent(Container container, CallbackInfo ci) {

        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        ItemStack result = resultSlots.getItem(0);
        if (result.isEmpty())
            return;

        ItemStack modified =
                CraftingPreviewEvent.fire(serverPlayer, craftSlots, result);

        resultSlots.setItem(0, modified);
    }
}