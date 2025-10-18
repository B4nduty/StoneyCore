package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CrossbowHandler implements IRangedWeaponHandler {
    @Override public String getTypeId() { return "crossbow"; }

    @Override
    public void shoot(World world, PlayerEntity player, ItemStack weapon) {
        if (world == null || player == null || weapon == null) return;

        var ws = SCRangeWeaponUtil.getWeaponState(weapon);
        if (!ws.isCharged()) return;

        NbtCompound nbt = weapon.getNbt();
        if (nbt == null || !nbt.contains("loaded_arrow")) return;

        Item arrowItem = Registries.ITEM.get(new Identifier(nbt.getString("loaded_arrow")));
        if (!(arrowItem instanceof ArrowItem arrow)) return;

        ItemStack fakeArrow = new ItemStack(arrow);

        SCRangeWeaponUtil.shootArrow(world, weapon, player, fakeArrow, 1.0f);

        nbt.remove("loaded_arrow");
        SCRangeWeaponUtil.setWeaponState(weapon, new SCRangeWeaponUtil.WeaponState(false, false, true));
    }


    @Override
    public void reload(World world, PlayerEntity player, ItemStack weapon) {
        SCRangeWeaponUtil.setWeaponState(weapon, new SCRangeWeaponUtil.WeaponState(true, false, false));
    }

    @Override
    public void handleRelease(ItemStack stack, World world, PlayerEntity player, int useTime, ItemStack arrowStack) {
        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack);
        if (pullProgress < 1.0F) {
            SCRangeWeaponUtil.WeaponState currentState = SCRangeWeaponUtil.getWeaponState(stack);
            SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(
                    false, currentState.isCharged(), currentState.isShooting()));
        }
    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        var s = SCRangeWeaponUtil.getWeaponState(weapon);
        return s.isCharged() && !s.isReloading();
    }

    @Override
    public void handleUsageTick(World world, ItemStack stack, PlayerEntity player, int useTime) {
        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack);
        SCRangeWeaponUtil.WeaponState weaponState = SCRangeWeaponUtil.getWeaponState(stack);

        if (pullProgress > 0.1F && pullProgress < 1.0F)
            SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(true, false, false));

        if (pullProgress >= 1.0F && !weaponState.isCharged()) {
            SCRangeWeaponUtil.getArrowFromInventory(player).ifPresent(arrowStack -> {
                if (arrowStack.getItem() instanceof ArrowItem) {
                    if (!player.isCreative()) {
                        int slot = SCRangeWeaponUtil.getArrowSlot(player);
                        if (slot >= 0) player.getInventory().removeStack(slot, 1);
                    }

                    NbtCompound nbt = stack.getOrCreateNbt();
                    nbt.putString("loaded_arrow", Registries.ITEM.getId(arrowStack.getItem()).toString());

                    SCRangeWeaponUtil.loadAndPlayCrossbowSound(world, stack, player);
                }
            });
        }
    }
}