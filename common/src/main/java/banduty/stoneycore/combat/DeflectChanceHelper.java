package banduty.stoneycore.combat;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.combat.weapon.SCArmorUtil;
import banduty.stoneycore.combat.weapon.SCWeaponUtil;
import banduty.stoneycore.config.IConfig;
import banduty.stoneycore.data.IEntityDataSaver;
import banduty.stoneycore.data.SCTags;
import banduty.stoneycore.entity.projectile.DamageTypedProjectile;
import banduty.stoneycore.mixin.AbstractArrowAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DeflectChanceHelper {
    public static boolean rollWithStreak(LivingEntity victim, double chance) {
        CompoundTag data = ((IEntityDataSaver) victim).stoneycore$getPersistentData();
        long now = victim.level().getGameTime();
        int streak = data.getInt("deflect_streak");
        IConfig.CombatOptions config = StoneyCore.getConfig().combatOptions();
        if (streak > 0 && now - data.getLong("deflect_streak_time") > config.getDeflectStreakResetTicks()) streak = 0;

        double decay = Math.clamp(config.getDeflectStreakDecay(), 0.0, 1.0);
        double effective = chance * Math.pow(decay, streak);
        if (effective <= 0 || victim.getRandom().nextDouble() >= effective) {
            data.putInt("deflect_streak", 0);
            return false;
        }
        data.putInt("deflect_streak", streak + 1);
        data.putLong("deflect_streak_time", now);
        return true;
    }

    public static boolean shouldDeflect(LivingEntity livingEntity) {
        return shouldDeflect(livingEntity, null);
    }

    public static boolean shouldDeflect(LivingEntity livingEntity, SCDamageType type) {
        double chance = SCWeaponUtil.total(SCArmorUtil.getDeflectChances(livingEntity), ItemStack.EMPTY, type, 0);
        return rollWithStreak(livingEntity, chance);
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

        SCDamageType type = projectile instanceof DamageTypedProjectile typed ? typed.getDamageType() : null;
        if (!shouldDeflect(target, type)) return false;

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
            DeflectSounds.play(projectile, type, volume);
        }
        return true;
    }
}