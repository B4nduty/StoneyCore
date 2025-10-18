package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MusketHandler implements IRangedWeaponHandler {
    @Override public String getTypeId() { return "musket"; }

    @Override
    public void shoot(World world, PlayerEntity player, ItemStack weapon) {
        if (!canShoot(weapon)) return;
        SCRangeWeaponUtil.shootBullet(world, weapon, player);
        SCRangeWeaponUtil.setWeaponState(weapon, new SCRangeWeaponUtil.WeaponState(false, false, true));
    }

    @Override
    public void reload(World world, PlayerEntity player, ItemStack weapon) {
        SCRangeWeaponUtil.setWeaponState(weapon, new SCRangeWeaponUtil.WeaponState(true, false, false));
    }

    @Override
    public void handleRelease(ItemStack stack, World world, PlayerEntity player, int useTime, ItemStack arrowStack) {

    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return !SCRangeWeaponUtil.getWeaponState(weapon).isReloading();
    }

    @Override
    public void handleUsageTick(World world, ItemStack stack, PlayerEntity player, int useTime) {

    }
}