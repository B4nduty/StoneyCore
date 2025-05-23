package banduty.stoneycore.mixin;

import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void stoneycore$pre_doAttack(CallbackInfoReturnable<Boolean> info) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (StaminaData.isStaminaBlocked((IEntityDataSaver) player)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
