package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BowHandler implements IRangedWeaponHandler {
    @Override
    public String getTypeId() {
        return "bow";
    }

    @Override
    public void shoot(Level level, Player player, ItemStack weapon) {
    }

    @Override
    public void reload(Level level, Player player, ItemStack weapon) { /* bows don't reload */ }

    @Override
    public void handleRelease(ItemStack stack, Level level, Player player, int useTime, ItemStack arrowStack) {
        if (level.isClientSide) return;

        ItemStack ammo = arrowStack;

        if (ammo.isEmpty()) {
            if (!player.isCreative()) return;
            ammo = new ItemStack(Items.ARROW);
        }

        float pull = SCRangeWeaponUtil.getBowPullProgress(useTime);
        if (pull < 0.1f) return;

        SCRangeWeaponUtil.shootArrow(level, stack, player, ammo, pull);

        if (!player.isCreative()) {
            arrowStack.shrink(1);
        }
    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return true;
    }

    @Override
    public void handleUsageTick(Level level, ItemStack stack, Player player, int useTime) {
    }
}