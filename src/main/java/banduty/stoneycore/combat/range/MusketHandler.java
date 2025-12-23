package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.SCInventoryItemFinder;
import banduty.stoneycore.util.servertick.MechanicsUtil;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MusketHandler implements IRangedWeaponHandler {
    @Override public String getTypeId() { return "musket"; }

    @Override
    public void shoot(Level level, Player player, ItemStack weapon) {
        if (!canShoot(weapon)) return;
        SCRangeWeaponUtil.shootBullet(level, weapon, player);
        SCRangeWeaponUtil.setWeaponState(weapon, new SCRangeWeaponUtil.WeaponState(false, false, true));
    }

    @Override
    public void reload(Level level, Player player, ItemStack weapon) {
        if (player instanceof ServerPlayer serverPlayer && player.isCreative()) {
            startReload(serverPlayer, weapon);
            return;
        }

        if (player instanceof ServerPlayer serverPlayer && hasRequiredAmmo(serverPlayer, weapon))
            startReload(serverPlayer, weapon);

    }

    private static boolean hasRequiredAmmo(ServerPlayer player, ItemStack itemStack) {
        var ammoReq = SCRangeWeaponUtil.getAmmoRequirement(itemStack);
        ItemStack[] foundItems = {
                SCInventoryItemFinder.getItemFromInventory(player, ammoReq.firstItem(), ammoReq.firstItem2nOption()),
                SCInventoryItemFinder.getItemFromInventory(player, ammoReq.secondItem(), ammoReq.secondItem2nOption()),
                SCInventoryItemFinder.getItemFromInventory(player, ammoReq.thirdItem(), ammoReq.thirdItem2nOption())
        };
        int[] requiredAmounts = {
                ammoReq.amountFirstItem(),
                ammoReq.amountSecondItem(),
                ammoReq.amountThirdItem()
        };
        return SCInventoryItemFinder.areItemsInInventory(foundItems, requiredAmounts);
    }

    private static void startReload(ServerPlayer player, ItemStack itemStack) {
        var state = SCRangeWeaponUtil.getWeaponState(itemStack);
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(true, state.isCharged(), false));
        MechanicsUtil.incrementRechargeTime(player);
    }

    @Override
    public void handleRelease(ItemStack stack, Level level, Player player, int useTime, ItemStack arrowStack) {

    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return !SCRangeWeaponUtil.getWeaponState(weapon).isReloading();
    }

    @Override
    public void handleUsageTick(Level level, ItemStack stack, Player player, int useTime) {

    }
}