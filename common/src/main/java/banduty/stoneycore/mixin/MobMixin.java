package banduty.stoneycore.mixin;

import banduty.stoneycore.combat.melee.CombatSelect;
import banduty.stoneycore.util.EntityDamageUtil;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
        if (!(target instanceof LivingEntity livingTarget) || livingTarget.level().isClientSide()) return originalBaseDamage;

        ItemStack weaponStack = CombatSelect.getWeaponStack(stoneycore$mob, stoneycore$mob.getMainHandItem());

        if (weaponStack == null || weaponStack.isEmpty() || weaponStack.is(Items.AIR)) {
            return originalBaseDamage;
        }
        if (!WeaponDefinitionsStorage.isMelee(weaponStack)) {
            return originalBaseDamage;
        }

        double damage = EntityDamageUtil.onDamage(livingTarget, stoneycore$mob, weaponStack);

        if (weaponStack.is(SCTags.BROKEN_WEAPONS.getTag()) && weaponStack.getDamageValue() >= weaponStack.getMaxDamage() * 0.9f) {
            damage *= 0.2f;
        }

        return (float) Math.max(0.0, damage);
    }
}