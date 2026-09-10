package banduty.stoneycore.combat.range;

import banduty.stoneycore.combat.weapon.SCRangeWeaponUtil;
import net.minecraft.world.entity.LivingEntity;
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
    public void shoot(Level level, LivingEntity livingEntity, ItemStack weapon) {
    }

    @Override
    public void reload(Level level, LivingEntity livingEntity, ItemStack weapon) { /* bows don't reload */ }

    @Override
    public void handleRelease(ItemStack stack, Level level, LivingEntity livingEntity, int useTime, ItemStack arrowStack) {
        if (level.isClientSide) return;

        ItemStack ammo = arrowStack;

        if (ammo.isEmpty()) {
            if (!(livingEntity instanceof Player player && player.isCreative())) return;
            ammo = new ItemStack(Items.ARROW);
        }

        float pull = SCRangeWeaponUtil.getBowPullProgress(useTime);
        if (pull < 0.1f) return;

        SCRangeWeaponUtil.shootArrow(level, stack, livingEntity, ammo, pull);

        if (!(livingEntity instanceof Player player && player.isCreative())) {
            arrowStack.shrink(1);
        }
    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return true;
    }

    @Override
    public void handleUsageTick(Level level, ItemStack stack, LivingEntity livingEntity, int useTime) {
    }
}