package banduty.stoneycore.mixin;

import banduty.stoneycore.items.armor.SCTrinketsItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

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

        AtomicReference<Double> totalHungerAddition = new AtomicReference<>(1.0d);

        TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent ->
                trinketComponent.getEquipped(stack -> stack.getItem() instanceof SCTrinketsItem).forEach(equipped -> {
                    ItemStack trinket = equipped.getRight();
                    SCTrinketsItem scTrinket = (SCTrinketsItem) trinket.getItem();
                    totalHungerAddition.updateAndGet(current -> current + scTrinket.hungerDrainAddition());
                }));

        float modifiedExhaustion = (float) Math.min(((HungerManager) (Object) this).getExhaustion() + exhaustion * totalHungerAddition.get(), 40.0F);
        player.getHungerManager().setExhaustion(modifiedExhaustion);

        ci.cancel();
    }
}