package banduty.stoneycore.mixin;

import banduty.stoneycore.util.WeightUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelWriter.class)
public interface LevelWriterMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    default void stoneycore$addFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LivingEntity livingEntity)
            WeightUtil.refreshWeight(livingEntity);
    }
}
