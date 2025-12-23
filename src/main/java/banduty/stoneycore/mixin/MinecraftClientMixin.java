package banduty.stoneycore.mixin;

import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void stoneycore$startAttack(CallbackInfoReturnable<Boolean> info) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (StaminaData.isStaminaBlocked((IEntityDataSaver) player)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
