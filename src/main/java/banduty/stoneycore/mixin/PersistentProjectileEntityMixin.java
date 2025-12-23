package banduty.stoneycore.mixin;

import banduty.stoneycore.util.DeflectChanceHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(AbstractArrow.class)
public abstract class PersistentProjectileEntityMixin {
    @Shadow protected abstract ItemStack getPickupItem();

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!(entityHitResult.getEntity().level() instanceof ServerLevel serverLevel)) return;
        AbstractArrow projectileEntity = (AbstractArrow) (Object) this;

        if (getPickupItem() == null || getPickupItem().isEmpty()) {
            return;
        }

        if (projectileEntity != null) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && DeflectChanceHelper.shouldDeflect(livingEntity, getPickupItem())) {
                projectileEntity.setDeltaMovement(projectileEntity.getDeltaMovement().scale(0.5).scale(-1));
                projectileEntity.setBaseDamage(projectileEntity.getBaseDamage() * 0.5);
                projectileEntity.setYRot(projectileEntity.getYRot() + 180);
                if (projectileEntity.getBaseDamage() <= 1) projectileEntity.discard();
                for (Player player : serverLevel.players()) {
                    if (player != null) {
                        Vec3 playerPos = player.position();
                        Vec3 impactPos = entityHitResult.getLocation();
                        double distance = playerPos.distanceTo(impactPos);

                        float volume = (float) Math.max(0, 1 - (distance * 0.1));

                        projectileEntity.playSound(SoundEvents.ANVIL_PLACE, volume, 2.5F / (new Random().nextFloat() + 0.5F));
                    }
                }
                ci.cancel();
            }
        }
    }
}
