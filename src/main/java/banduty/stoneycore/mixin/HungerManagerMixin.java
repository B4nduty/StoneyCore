package banduty.stoneycore.mixin;

import banduty.stoneycore.util.playerdata.SCAttributes;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {
    @Unique
    private PlayerEntity player;

    @Inject(method = "update", at = @At("HEAD"))
    public void stoneycore$update(PlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }

    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    public void stoneycore$onAddExhaustion(float exhaustion, CallbackInfo ci) {
        if (player == null) return;

        double totalHungerAddition = 1;

        if (player.getAttributeInstance(SCAttributes.HUNGER_DRAIN_MULTIPLIER) != null) {
            totalHungerAddition += player.getAttributeInstance(SCAttributes.HUNGER_DRAIN_MULTIPLIER).getValue();
        }

        float modifiedExhaustion = (float) Math.min(((HungerManager) (Object) this).getExhaustion() + exhaustion * totalHungerAddition, 40.0F);
        player.getHungerManager().setExhaustion(modifiedExhaustion);

        ci.cancel();
    }
}