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
    @Override public String getTypeId() { return "crossbow"; }

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
        SCRangeWeaponUtil.WeaponState weaponState = SCRangeWeaponUtil.getWeaponState(stack);

        if (pullProgress < 1.0F && !weaponState.isCharged())
            SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(true, false, false));

        if (pullProgress >= 1.0F) {
            if (!weaponState.isCharged()) {
                SCRangeWeaponUtil.getArrowFromInventory(player).ifPresent(arrowStack -> {
                    if (arrowStack.getItem() instanceof ArrowItem) {
                        if (!player.isCreative()) {
                            int slot = SCRangeWeaponUtil.getArrowSlot(player);
                            if (slot >= 0) player.getInventory().removeItem(slot, 1);
                        }

                        CompoundTag nbt = stack.getOrCreateTag();
                        nbt.putString("loaded_arrow", BuiltInRegistries.ITEM.getKey(arrowStack.getItem()).toString());

                        SCRangeWeaponUtil.loadAndPlayCrossbowSound(level, stack, player);
                    }
                });
            }
            return;
        }

        if (weaponState.isCharged() && pullProgress < 1.0F) {
            shoot(level, player, stack);
            SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(false, false, true));
        }
    }
}