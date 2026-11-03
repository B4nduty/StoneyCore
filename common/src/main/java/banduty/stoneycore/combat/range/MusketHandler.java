package banduty.stoneycore.combat.range;

import banduty.stoneycore.combat.mechanics.MechanicsUtil;
import banduty.stoneycore.combat.weapon.SCRangeWeaponUtil;
import banduty.stoneycore.combat.weapon.SCRangeWeaponUtil.WeaponState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MusketHandler implements IRangedWeaponHandler {
    @Override
    public String getTypeId() {
        return "musket";
    }

    @Override
    public void shoot(Level level, LivingEntity livingEntity, ItemStack weapon) {
        if (!canShoot(weapon)) return;
        SCRangeWeaponUtil.shootBullet(level, weapon, livingEntity);
        SCRangeWeaponUtil.setWeaponState(weapon, WeaponState.shooting());
    }

    @Override
    public void reload(Level level, LivingEntity livingEntity, ItemStack weapon) {
        if (!(livingEntity instanceof ServerPlayer player)) return;
        if (!player.isCreative() && !SCRangeWeaponUtil.hasRequiredAmmo(player, SCRangeWeaponUtil.getAmmoRequirement(weapon))) return;

        WeaponState state = SCRangeWeaponUtil.getWeaponState(weapon);
        SCRangeWeaponUtil.setWeaponState(weapon, new WeaponState(true, state.isCharged(), false));
        MechanicsUtil.incrementRechargeTime(player);
    }

    @Override
    public void handleRelease(ItemStack stack, Level level, LivingEntity livingEntity, int useTime, ItemStack arrowStack) {
    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return !SCRangeWeaponUtil.getWeaponState(weapon).isReloading();
    }

    @Override
    public void handleUsageTick(Level level, ItemStack stack, LivingEntity livingEntity, int useTime) {
    }
}
