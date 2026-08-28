package banduty.stoneycore.mixin;

import banduty.stoneycore.mobgear.SCMobGearHandler;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobGearMixin {
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void stoneycore$rollCustomGear(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        SCMobGearHandler.tryEquipRandomGear((Mob) (Object) this, random, difficulty);
    }
}