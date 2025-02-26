package banduty.stoneycore.event;

import banduty.stoneycore.items.item.SCRangeWeapon;
import banduty.stoneycore.util.SCInventoryItemFinder;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ReloadTickHandler implements ServerTickEvents.StartTick {
    private ItemStack lastItemStack;

    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handlePlayerReload(player);
        }
    }

    private void handlePlayerReload(ServerPlayerEntity player) {
        ItemStack currentItem = player.getMainHandStack();

        if (currentItem != lastItemStack) {
            if (lastItemStack != null) {
                if (lastItemStack.getItem() instanceof SCRangeWeapon) {
                    lastItemStack.getOrCreateNbt().putBoolean("sc_recharge", false);
                    resetWeaponState(player, lastItemStack);
                }
            }
            lastItemStack = currentItem;
            return;
        }

        if (currentItem.getNbt() == null) return;

        if (!isReloading(currentItem)) {
            resetRechargeTime(player);
            return;
        }

        if (!isValidRangeWeapon(currentItem)) {
            resetRechargeTime(player);
            return;
        }

        if (player.isCreative()) {
            completeReload(player, currentItem);
            return;
        }

        Item weapon = currentItem.getItem();
        if (player.getItemCooldownManager().isCoolingDown(weapon)) {
            return;
        }

        if (SCRangeWeaponUtil.getWeaponState(currentItem).isCharged()) {
            return;
        }

        if (SCRangeWeaponUtil.getWeaponState(currentItem).isReloading()) {
            incrementRechargeTime(player);
        } else if (hasRequiredAmmo(player, (SCRangeWeapon) weapon)) {
            startReload(player, currentItem);
        } else {
            currentItem.getOrCreateNbt().putBoolean("sc_recharge", false);
        }

        if (getRechargeTime(player) >= ((SCRangeWeapon) weapon).rechargeTime() * 20) {
            completeReload(player, currentItem);
        }
    }

    private void resetWeaponState(ServerPlayerEntity player, ItemStack itemStack) {
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(false, SCRangeWeaponUtil.getWeaponState(itemStack).isCharged(), false));
        setRechargeTime(player, 0);
    }

    private boolean isReloading(ItemStack itemStack) {
        return itemStack.getOrCreateNbt().getBoolean("sc_recharge");
    }

    private boolean isValidRangeWeapon(ItemStack itemStack) {
        return itemStack.getItem() instanceof SCRangeWeapon && ((SCRangeWeapon) itemStack.getItem()).ammoRequirement() != null;
    }

    private boolean hasRequiredAmmo(ServerPlayerEntity player, SCRangeWeapon weapon) {
        ItemStack[] requiredItems = {
                SCInventoryItemFinder.getItemFromInventory(player, weapon.ammoRequirement().firstItem(), weapon.ammoRequirement().firstItem2nOption()),
                SCInventoryItemFinder.getItemFromInventory(player, weapon.ammoRequirement().secondItem(), weapon.ammoRequirement().secondItem2nOption()),
                SCInventoryItemFinder.getItemFromInventory(player, weapon.ammoRequirement().thirdItem(), weapon.ammoRequirement().thirdItem2nOption())
        };

        int[] requiredAmounts = {
                weapon.ammoRequirement().amountFirstItem(),
                weapon.ammoRequirement().amountSecondItem(),
                weapon.ammoRequirement().amountThirdItem()
        };

        return SCInventoryItemFinder.areItemsInInventory(requiredItems, requiredAmounts);
    }

    private void startReload(ServerPlayerEntity player, ItemStack itemStack) {
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(true, SCRangeWeaponUtil.getWeaponState(itemStack).isCharged(), false));
        incrementRechargeTime(player);
    }

    private void completeReload(ServerPlayerEntity player, ItemStack itemStack) {
        if (!player.isCreative()) {
            SCRangeWeapon weapon = ((SCRangeWeapon) itemStack.getItem());
            ItemStack[] ammoItems = {
                    SCInventoryItemFinder.getItemFromInventory(player, weapon.ammoRequirement().firstItem(), weapon.ammoRequirement().firstItem2nOption()),
                    SCInventoryItemFinder.getItemFromInventory(player, weapon.ammoRequirement().secondItem(), weapon.ammoRequirement().secondItem2nOption()),
                    SCInventoryItemFinder.getItemFromInventory(player, weapon.ammoRequirement().thirdItem(), weapon.ammoRequirement().thirdItem2nOption())
            };

            for (ItemStack ammoItem : ammoItems) {
                player.getInventory().removeStack(SCInventoryItemFinder.getItemSlot(player, ammoItem), 1);
            }
        }

        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(false, true, false));
        setRechargeTime(player, 0);
        itemStack.getOrCreateNbt().putBoolean("sc_recharge", false);
    }

    private void resetRechargeTime(PlayerEntity player) {
        setRechargeTime(player, 0);
    }

    private void incrementRechargeTime(PlayerEntity player) {
        setRechargeTime(player, getRechargeTime(player) + 1);
    }

    private int getRechargeTime(PlayerEntity player) {
        return ((IEntityDataSaver) player).stoneycore$getPersistentData().getInt("rechargeTime");
    }

    private void setRechargeTime(PlayerEntity player, int time) {
        ((IEntityDataSaver) player).stoneycore$getPersistentData().putInt("rechargeTime", time);
    }
}