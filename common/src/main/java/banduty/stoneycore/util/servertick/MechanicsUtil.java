package banduty.stoneycore.util.servertick;

import banduty.stoneycore.util.SCInventoryItemFinder;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class MechanicsUtil {
    private static final Map<LivingEntity, ItemStack> LAST_ITEMSTACK_MAP =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static void handleParry(ServerPlayer player) {
        if (NBTDataHelper.get((IEntityDataSaver) player, PDKeys.BLOCK_START_TICK, 0L) > 0) return;
        ItemStack activeItem = player.getUseItem();
        boolean isBlocking = player.isBlocking();
        boolean usingCustomShield = activeItem.is(SCTags.WEAPONS_SHIELD.getTag());

        if (isBlocking && usingCustomShield) {
            NBTDataHelper.set((IEntityDataSaver) player, PDKeys.BLOCK_START_TICK, player.level().getGameTime());
        } else {
            NBTDataHelper.set((IEntityDataSaver) player, PDKeys.BLOCK_START_TICK, 0L);
        }
    }

    public static void handlePlayerReload(ServerPlayer player) {
        ItemStack currentItem = player.getMainHandItem();
        CompoundTag tag = currentItem.getTag();

        if (SCRangeWeaponUtil.getAmmoRequirement(currentItem) == SCRangeWeaponUtil.AmmoRequirement.EMPTY) return;

        var weaponState = SCRangeWeaponUtil.getWeaponState(currentItem);

        if (weaponState.isReloading()) {
            player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
            player.hurtMarked = true;
        }

        ItemStack lastItem = LAST_ITEMSTACK_MAP.get(player);
        if (currentItem != lastItem) {
            if (lastItem != null && lastItem.getTag() != null && WeaponDefinitionsStorage.isRanged(lastItem)) {
                SCRangeWeaponUtil.setWeaponState(currentItem, new SCRangeWeaponUtil.WeaponState(false, weaponState.isCharged(), false));
                resetWeaponState(player, lastItem);
            }
            LAST_ITEMSTACK_MAP.put(player, currentItem);
            return;
        }

        if (tag == null || !SCRangeWeaponUtil.getWeaponState(currentItem).isReloading() || !WeaponDefinitionsStorage.isRanged(currentItem)) {
            resetRechargeTime(player);
            return;
        }

        if (player.isCreative()) {
            completeReload(player, currentItem);
            return;
        }

        if (player.getCooldowns().isOnCooldown(currentItem.getItem()) || weaponState.isCharged()) return;

        if (weaponState.isReloading()) {
            incrementRechargeTime(player);
        }

        int requiredTicks = WeaponDefinitionsStorage.getData(currentItem).ranged().rechargeTime() * 20;
        if (getRechargeTime(player) >= requiredTicks) {
            completeReload(player, currentItem);
        }
    }

    private static void completeReload(ServerPlayer player, ItemStack itemStack) {
        if (!player.isCreative()) {
            var ammoReq = SCRangeWeaponUtil.getAmmoRequirement(itemStack);
            ItemStack[] ammoItems = {
                    SCInventoryItemFinder.getItemFromInventory(player, ammoReq.firstItem(), ammoReq.firstItem2nOption()),
                    SCInventoryItemFinder.getItemFromInventory(player, ammoReq.secondItem(), ammoReq.secondItem2nOption()),
                    SCInventoryItemFinder.getItemFromInventory(player, ammoReq.thirdItem(), ammoReq.thirdItem2nOption())
            };

            for (ItemStack ammoItem : ammoItems) {
                if (ammoItem != null && !ammoItem.isEmpty()) {
                    int slot = SCInventoryItemFinder.getItemSlot(player, ammoItem);
                    if (slot >= 0 && slot < player.getInventory().getContainerSize()) {
                        player.getInventory().removeItem(slot, 1);
                    }
                }
            }
        }

        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(false, true, false));
        setRechargeTime(player, 0);
    }

    private static void resetWeaponState(ServerPlayer player, ItemStack itemStack) {
        var state = SCRangeWeaponUtil.getWeaponState(itemStack);
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(false, state.isCharged(), false));
        setRechargeTime(player, 0);
    }

    private static void resetRechargeTime(Player player) {
        setRechargeTime(player, 0);
    }

    public static void incrementRechargeTime(Player player) {
        setRechargeTime(player, getRechargeTime(player) + 1);
    }

    private static int getRechargeTime(Player player) {
        return NBTDataHelper.get((IEntityDataSaver) player, PDKeys.RECHARGE_TIME, 0);
    }

    private static void setRechargeTime(Player player, int time) {
        NBTDataHelper.set((IEntityDataSaver) player, PDKeys.RECHARGE_TIME, time);
    }
}