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
        if (world == null || player == null || weapon == null) return;
        Optional<ItemStack> arrow = SCRangeWeaponUtil.getArrowFromInventory(player);
        if (arrow.isEmpty()) return;
        SCRangeWeaponUtil.shootArrow(world, weapon, player, arrow.get(), 1.0f);
    }

    @Override public void reload(World world, PlayerEntity player, ItemStack weapon) { /* bows don't reload */ }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return !SCRangeWeaponUtil.getWeaponState(weapon).isReloading();
    }

    @Override
    public void handleUsageTick(World world, ItemStack stack, PlayerEntity player, int useTime) {

    }
}