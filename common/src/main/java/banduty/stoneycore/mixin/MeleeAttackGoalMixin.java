package banduty.stoneycore.mixin;

import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    protected abstract boolean isTimeToAttack();

    @Unique
    private boolean stoneycore$hasAttackReach = false;

    @Unique
    private double stoneycore$currentRandomReach = 0.0;

    @Inject(method = "canPerformAttack", at = @At("HEAD"), cancellable = true)
    private void modifyAttackDistanceThreshold(LivingEntity enemy, CallbackInfoReturnable<Boolean> cir) {
        Item item = this.mob.getMainHandItem().getItem();
        if (!WeaponDefinitionsStorage.isMelee(item)) return;
        if (this.mob instanceof Zombie zombie) {
            // Choose reach distance with more chance on max damage distance
            if (!stoneycore$hasAttackReach) {
                SCDamageType damageType = SCWeaponUtil.calculateDamageType(this.mob.getMainHandItem(), 0);
                if (SCWeaponUtil.hasDamageType(SCDamageType.PIERCING, item)) damageType = SCDamageType.PIERCING;
                else if (SCWeaponUtil.hasDamageType(SCDamageType.BLUDGEONING, item)) damageType = SCDamageType.BLUDGEONING;

                // Calculate total weight
                double totalWeight = 0.0;
                int validLevels = 0;
                WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
                for (int i = 0; i < attributeData.melee().radius().size(); i++) {
                    if (SCWeaponUtil.getRadius(item, i) <= 0.0) break;
                    totalWeight += SCWeaponUtil.getDamageValues(damageType, item, i) + 1.0;
                    validLevels++;
                }

                // Roll and choose selection
                if (validLevels > 0) {
                    double roll = zombie.getRandom().nextDouble() * totalWeight;
                    double currentWeightSum = 0.0;

                    // Fallback default to level 0
                    stoneycore$currentRandomReach = SCWeaponUtil.getRadius(item, 0);

                    for (int i = 0; i < validLevels; i++) {
                        currentWeightSum += SCWeaponUtil.getDamageValues(damageType, item, i) + 1.0;
                        if (roll <= currentWeightSum) {
                            stoneycore$currentRandomReach = SCWeaponUtil.getRadius(item, i);
                            break;
                        }
                    }
                    // Apply micro-variance for natural behavior
                    stoneycore$currentRandomReach += (zombie.getRandom().nextDouble() - 0.5) * 0.1;
                } else {
                    stoneycore$currentRandomReach = SCWeaponUtil.getMaxDistance(item) * zombie.getRandom().nextDouble();
                }

                stoneycore$currentRandomReach = Math.max(stoneycore$currentRandomReach, 0.5);
                stoneycore$hasAttackReach = true;
            }

            // Check distance against our rolled reach
            double actualDistance = zombie.distanceTo(enemy);

            // Evaluate if attack conditions are met
            boolean canAttack = isTimeToAttack()
                    && actualDistance <= stoneycore$currentRandomReach
                    && this.mob.getSensing().hasLineOfSight(enemy);

            // Reset when the attack successfully executes
            if (canAttack) {
                stoneycore$hasAttackReach = false;
            }

            cir.setReturnValue(canAttack);
        }
    }
}