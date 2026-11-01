package banduty.stoneycore.combat;

import banduty.stoneycore.data.SCAttributes;
import banduty.stoneycore.data.SCTags;
import banduty.stoneycore.mixin.AbstractArrowAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class DeflectChanceHelper {
    private static final Random random = new Random();

    public static boolean shouldDeflect(LivingEntity livingEntity) {
        double totalDeflect = livingEntity.getAttributeValue(SCAttributes.DEFLECT_CHANCE);
        return totalDeflect > random.nextDouble();
    }

    public static boolean isDeflectable(Projectile projectile) {
        return projectile.getType().is(SCTags.Entity.DEFLECTABLE_PROJECTILES.getTag());
    }

    public static boolean tryDeflect(Projectile projectile, EntityHitResult hit) {
        if (!(projectile.level() instanceof ServerLevel serverLevel)) return false;
        if (!isDeflectable(projectile)) return false;
        if (!(hit.getEntity() instanceof LivingEntity target)) return false;

        if (projectile instanceof AbstractArrow arrow && ((AbstractArrowAccessor) arrow).stoneycore$getPickupItem().isEmpty()) {
            return false;
        }

        if (!shouldDeflect(target)) return false;

        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.5));
        projectile.setYRot(projectile.getYRot() + 180);
        projectile.hasImpulse = true;

        if (projectile instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(arrow.getBaseDamage() * 0.5);
            if (arrow.getBaseDamage() <= 1) {
                arrow.discard();
            }
        }

        Vec3 impactPos = hit.getLocation();
        for (Player player : serverLevel.players()) {
            double distance = player.position().distanceTo(impactPos);
            float volume = (float) Math.max(0, 1 - (distance * 0.1));
            projectile.playSound(SoundEvents.ANVIL_PLACE, volume, 2.5F / (random.nextFloat() * 0.2F + 0.9F));
        }
        return true;
    }
}