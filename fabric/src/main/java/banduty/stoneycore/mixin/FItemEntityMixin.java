package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.PlayerPickupCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class FItemEntityMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onPlayerPickup(Player player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;

        InteractionResult result = PlayerPickupCallback.EVENT.invoker().onPickup(player, self);

        if (result == InteractionResult.FAIL) {
            ci.cancel(); // cancel pickup
        }
    }
}