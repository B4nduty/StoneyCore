package banduty.stoneycore.mixin;

import banduty.stoneycore.lands.visitor.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMixin {
    @Inject(method = "updateTrades", at = @At("HEAD"))
    private void onUpdateTrades(CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        Level level = villager.level();

        if (level instanceof ServerLevel serverLevel) {
            VisitorTracker.onTradeCompleted(villager, serverLevel);
        }
    }
}