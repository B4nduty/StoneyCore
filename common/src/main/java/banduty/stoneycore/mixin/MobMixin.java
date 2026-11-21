package banduty.stoneycore.mixin;

import banduty.stoneycore.combat.melee.CombatSelect;
import banduty.stoneycore.combat.damagetype.EntityDamageUtil;
import banduty.stoneycore.data.IEntityDataSaver;
import banduty.stoneycore.data.SCTags;
import banduty.stoneycore.definitions.WeaponDefinitionsStorage;
import banduty.stoneycore.stamina.StaminaData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Unique
    private final Mob stoneycore$mob = (Mob)(Object)this;

    @ModifyVariable(
            method = "doHurtTarget",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            ordinal = 0
    )
    private float stoneycore$modifyMobDamage(float originalBaseDamage, Entity target) {
        if (!stoneycore$usesSCMeleeDamage(target)) return originalBaseDamage;

        LivingEntity livingTarget = (LivingEntity) target;
        ItemStack weaponStack = CombatSelect.getWeaponStack(stoneycore$mob, stoneycore$mob.getMainHandItem());

        double damage = EntityDamageUtil.onDamage(livingTarget, stoneycore$mob, weaponStack);

        if (weaponStack.is(SCTags.Item.BROKEN_WEAPONS.getTag()) && weaponStack.getDamageValue() >= weaponStack.getMaxDamage() * 0.9f) {
            damage *= 0.2f;
        }

        return (float) Math.max(0.0, damage);
    }

    /**
     * StoneyCore melee damage already has the weapon's enchantments applied (before armor resistances) in
     * DamageResult, so skip vanilla's enchantment pass to avoid counting it twice.
     */
    @WrapOperation(
            method = "doHurtTarget",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"
            )
    )
    private float stoneycore$skipVanillaEnchantedDamage(ServerLevel level, ItemStack stack, Entity target, DamageSource source, float damage, Operation<Float> original) {
        if (stoneycore$usesSCMeleeDamage(target)) return damage;
        return original.call(level, stack, target, source, damage);
    }

    @Unique
    private boolean stoneycore$usesSCMeleeDamage(Entity target) {
        if (!(target instanceof LivingEntity livingTarget) || livingTarget.level().isClientSide()) return false;

        ItemStack weaponStack = CombatSelect.getWeaponStack(stoneycore$mob, stoneycore$mob.getMainHandItem());
        if (weaponStack == null || weaponStack.isEmpty() || weaponStack.is(Items.AIR)) return false;

        return WeaponDefinitionsStorage.isMelee(weaponStack);
    }

    @Unique private static final double COMBAT_RADIUS_SQR = 16.0 * 16.0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void stoneycore$combatTick(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (self.level().isClientSide() || self.tickCount % 10 != 0) return;
        if (!(self instanceof Enemy)) return;

        if (self.getTarget() instanceof Entity entity
                && self.distanceToSqr(entity) <= COMBAT_RADIUS_SQR) {
            IEntityDataSaver entityDataSaver = (IEntityDataSaver) entity;
            StaminaData.setCombatMode(entityDataSaver, 8 * 20);
        }
    }
}
