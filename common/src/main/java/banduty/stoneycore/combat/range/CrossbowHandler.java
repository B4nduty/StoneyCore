package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrossbowHandler implements IRangedWeaponHandler {
    @Override
    public String getTypeId() {
        return "crossbow";
    }

    @Override
    public void shoot(Level level, Player player, ItemStack weapon) {
        if (level == null || player == null || weapon == null) return;

        var weaponState = SCRangeWeaponUtil.getWeaponState(weapon);
        if (!weaponState.isCharged()) return;

        CompoundTag nbt = weapon.getTag();
        if (nbt == null || !nbt.contains("loaded_arrow")) return;

        Item arrowItem = BuiltInRegistries.ITEM.get(new ResourceLocation(nbt.getString("loaded_arrow")));
        if (!(arrowItem instanceof ArrowItem arrow)) return;

        ItemStack fakeArrow = new ItemStack(arrow);

        SCRangeWeaponUtil.shootArrow(level, weapon, player, fakeArrow, 1.0f);

        nbt.remove("loaded_arrow");
    }

    @Override
    public void reload(Level level, Player player, ItemStack weapon) {

    }

    @Override
    public void handleRelease(ItemStack stack, Level level, Player player, int useTime, ItemStack arrowStack) {
        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack);
        if (pullProgress < 1.0F) {
            SCRangeWeaponUtil.WeaponState currentState = SCRangeWeaponUtil.getWeaponState(stack);
            SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(
                    false, currentState.isCharged(), currentState.isShooting()));
        }
    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return true;
    }

    @Override
    public void handleUsageTick(Level level, ItemStack stack, Player player, int useTime) {

        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack);
        SCRangeWeaponUtil.WeaponState state = SCRangeWeaponUtil.getWeaponState(stack);

        boolean hasAmmo = SCRangeWeaponUtil.getArrowFromInventory(player).isPresent();

        if (player.isCreative()) hasAmmo = true;

        if (pullProgress < 1.0F) {
            if (state.isCharged()) {
                shoot(level, player, stack);
                SCRangeWeaponUtil.setWeaponState(stack,
                        new SCRangeWeaponUtil.WeaponState(false, false, true));
            } else if (hasAmmo) {
                SCRangeWeaponUtil.setWeaponState(stack,
                        new SCRangeWeaponUtil.WeaponState(true, false, false));
            } else {
                SCRangeWeaponUtil.setWeaponState(stack,
                        new SCRangeWeaponUtil.WeaponState(false, false, false));
            }
        }

        if (pullProgress >= 1.0F) {
            if (!state.isCharged() && hasAmmo) {
                ItemStack ammoStack = SCRangeWeaponUtil
                        .getArrowFromInventory(player)
                        .orElseGet(() -> new ItemStack(net.minecraft.world.item.Items.ARROW));
                if (!player.isCreative()) {
                    int slot = SCRangeWeaponUtil.getArrowSlot(player);
                    if (slot >= 0) player.getInventory().removeItem(slot, 1);
                }
                CompoundTag nbt = stack.getOrCreateTag();
                nbt.putString("loaded_arrow", BuiltInRegistries.ITEM.getKey(ammoStack.getItem()).toString());
                SCRangeWeaponUtil.loadAndPlayCrossbowSound(level, stack, player);
            }
        }
    }
}