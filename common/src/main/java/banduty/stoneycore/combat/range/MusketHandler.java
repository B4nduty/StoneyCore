package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.SCInventoryItemFinder;
import banduty.stoneycore.combat.mechanics.MechanicsUtil;
import banduty.stoneycore.combat.weapon.SCRangeWeaponUtil;
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
        SCRangeWeaponUtil.setWeaponState(weapon, new SCRangeWeaponUtil.WeaponState(false, false, true));
    }

    @Override
    public void reload(Level level, LivingEntity livingEntity, ItemStack weapon) {
        if (livingEntity instanceof ServerPlayer serverPlayer && serverPlayer.isCreative()) {
            startReload(serverPlayer, weapon);
            return;
        }

        if (livingEntity instanceof ServerPlayer serverPlayer && hasRequiredAmmo(serverPlayer, weapon))
            startReload(serverPlayer, weapon);

    }

    private static boolean hasRequiredAmmo(ServerPlayer livingEntity, ItemStack weapon) {
        var ammoReq = SCRangeWeaponUtil.getAmmoRequirement(weapon);

        if (SCInventoryItemFinder.countItem(livingEntity, ammoReq.firstItem(), ammoReq.firstItem2nOption())
                < ammoReq.amountFirstItem()) return false;

        if (SCInventoryItemFinder.countItem(livingEntity, ammoReq.secondItem(), ammoReq.secondItem2nOption())
                < ammoReq.amountSecondItem()) return false;

        return SCInventoryItemFinder.countItem(livingEntity, ammoReq.thirdItem(), ammoReq.thirdItem2nOption()) >= ammoReq.amountThirdItem();
    }

    private static void startReload(ServerPlayer livingEntity, ItemStack itemStack) {
        var state = SCRangeWeaponUtil.getWeaponState(itemStack);
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(true, state.isCharged(), false));
        MechanicsUtil.incrementRechargeTime(livingEntity);
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