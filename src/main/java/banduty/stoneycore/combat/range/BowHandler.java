package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Optional;

public class BowHandler implements IRangedWeaponHandler {
    @Override public String getTypeId() { return "bow"; }

    @Override
    public void shoot(World world, PlayerEntity player, ItemStack weapon) {
    }

    @Override public void reload(World world, PlayerEntity player, ItemStack weapon) { /* bows don't reload */ }

    @Override
    public void handleRelease(ItemStack stack, World world, PlayerEntity player, int useTime, ItemStack arrowStack) {
        if (world == null || player == null || stack == null) return;
        float pullProgress = SCRangeWeaponUtil.getBowPullProgress(useTime);
        if (pullProgress < 0.1f) return;
        Optional<ItemStack> arrow = SCRangeWeaponUtil.getArrowFromInventory(player);
        if (arrow.isEmpty()) return;
        SCRangeWeaponUtil.shootArrow(world, stack, player, arrow.get(), 1.0f);
        if (!player.isCreative()) arrow.get().decrement(1);
    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return !SCRangeWeaponUtil.getWeaponState(weapon).isReloading();
    }

    @Override
    public void handleUsageTick(World world, ItemStack stack, PlayerEntity player, int useTime) {

    }
}