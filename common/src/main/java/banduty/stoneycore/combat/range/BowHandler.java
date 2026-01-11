package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class BowHandler implements IRangedWeaponHandler {
    @Override public String getTypeId() { return "bow"; }

    @Override
    public void shoot(Level level, Player player, ItemStack weapon) {
    }

    @Override public void reload(Level level, Player player, ItemStack weapon) { /* bows don't reload */ }

    @Override
    public void handleRelease(ItemStack stack, Level level, Player player, int useTime, ItemStack arrowStack) {
        if (level == null || player == null || stack == null) return;
        float pullProgress = SCRangeWeaponUtil.getBowPullProgress(useTime);
        if (pullProgress < 0.1f) return;
        Optional<ItemStack> arrow = SCRangeWeaponUtil.getArrowFromInventory(player);
        if (arrow.isEmpty()) return;
        SCRangeWeaponUtil.shootArrow(level, stack, player, arrow.get(), 1.0f);
        if (!player.isCreative()) arrow.get().shrink(1);
    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return !SCRangeWeaponUtil.getWeaponState(weapon).isReloading();
    }

    @Override
    public void handleUsageTick(Level level, ItemStack stack, Player player, int useTime) {

    }
}