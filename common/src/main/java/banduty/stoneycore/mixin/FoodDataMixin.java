package banduty.stoneycore.mixin;

import banduty.stoneycore.platform.Services;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Unique
    private Player player;

    @Inject(method = "tick", at = @At("HEAD"))
    public void stoneycore$update(Player player, CallbackInfo ci) {
        this.player = player;
    }

    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    public void stoneycore$onAddExhaustion(float exhaustion, CallbackInfo ci) {
        if (player == null) return;

        double totalHungerAddition = 1;

        if (player.getAttribute(Services.ATTRIBUTES.getHungerDrainMultiplier()) != null) {
            totalHungerAddition += player.getAttributeValue(Services.ATTRIBUTES.getHungerDrainMultiplier());
        }

        float modifiedExhaustion = (float) Math.min(((FoodData) (Object) this).getExhaustionLevel() + exhaustion * totalHungerAddition, 40.0F);
        player.getFoodData().setExhaustion(modifiedExhaustion);

        ci.cancel();
    }
}