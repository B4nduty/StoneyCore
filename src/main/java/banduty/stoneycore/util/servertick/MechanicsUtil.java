package banduty.stoneycore.util.servertick;

import banduty.stoneycore.util.SCInventoryItemFinder;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class MechanicsUtil {
    private static final Map<LivingEntity, ItemStack> LAST_ITEMSTACK_MAP =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static void handleParry(ServerPlayerEntity player) {
        if (NBTDataHelper.get((IEntityDataSaver) player, PDKeys.BLOCK_START_TICK, 0L) > 0) return;
        ItemStack activeItem = player.getActiveItem();
        boolean isBlocking = player.isBlocking();
        boolean usingCustomShield = activeItem.isIn(SCTags.WEAPONS_SHIELD.getTag());

        if (isBlocking && usingCustomShield) {
            NBTDataHelper.set((IEntityDataSaver) player, PDKeys.BLOCK_START_TICK, player.getWorld().getTime());
        } else {
            NBTDataHelper.set((IEntityDataSaver) player, PDKeys.BLOCK_START_TICK, 0L);
        }
    }

    public static void handlePlayerReload(ServerPlayerEntity player) {
        ItemStack currentItem = player.getMainHandStack();
        NbtCompound nbt = currentItem.getNbt();

        var weaponState = SCRangeWeaponUtil.getWeaponState(currentItem);

        if (weaponState.isReloading()) {
            player.setVelocity(0, player.getVelocity().y, 0);
            player.velocityDirty = true;
        }

        ItemStack lastItem = LAST_ITEMSTACK_MAP.get(player);
        if (currentItem != lastItem) {
            if (lastItem != null && lastItem.getNbt() != null && WeaponDefinitionsLoader.isRanged(lastItem)) {
                NBTDataHelper.set(lastItem, INBTKeys.RECHARGE, false);
                resetWeaponState(player, lastItem);
            }
            LAST_ITEMSTACK_MAP.put(player, currentItem);
            return;
        }

        if (nbt == null || !isReloading(currentItem) || !WeaponDefinitionsLoader.isRanged(currentItem)) {
            resetRechargeTime(player);
            return;
        }

        if (player.isCreative()) {
            completeReload(player, currentItem);
            return;
        }

        if (player.getItemCooldownManager().isCoolingDown(currentItem.getItem()) || weaponState.isCharged()) return;

        if (weaponState.isReloading()) {
            incrementRechargeTime(player);
        } else if (hasRequiredAmmo(player, currentItem)) {
            startReload(player, currentItem);
        } else {
            NBTDataHelper.set(currentItem, INBTKeys.RECHARGE, false);
        }

        int requiredTicks = WeaponDefinitionsLoader.getData(currentItem).ranged().rechargeTime() * 20;
        if (getRechargeTime(player) >= requiredTicks) {
            completeReload(player, currentItem);
        }
    }

    private static boolean isReloading(ItemStack itemStack) {
        return NBTDataHelper.get(itemStack, INBTKeys.RECHARGE, false);
    }

    private static boolean hasRequiredAmmo(ServerPlayerEntity player, ItemStack itemStack) {
        var ammoReq = SCRangeWeaponUtil.getAmmoRequirement(itemStack);
        ItemStack[] foundItems = {
                SCInventoryItemFinder.getItemFromInventory(player, ammoReq.firstItem(), ammoReq.firstItem2nOption()),
                SCInventoryItemFinder.getItemFromInventory(player, ammoReq.secondItem(), ammoReq.secondItem2nOption()),
                SCInventoryItemFinder.getItemFromInventory(player, ammoReq.thirdItem(), ammoReq.thirdItem2nOption())
        };
        int[] requiredAmounts = {
                ammoReq.amountFirstItem(),
                ammoReq.amountSecondItem(),
                ammoReq.amountThirdItem()
        };
        return SCInventoryItemFinder.areItemsInInventory(foundItems, requiredAmounts);
    }

    private static void startReload(ServerPlayerEntity player, ItemStack itemStack) {
        var state = SCRangeWeaponUtil.getWeaponState(itemStack);
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(true, state.isCharged(), false));
        incrementRechargeTime(player);
    }

    private static void completeReload(ServerPlayerEntity player, ItemStack itemStack) {
        if (!player.isCreative()) {
            var ammoReq = SCRangeWeaponUtil.getAmmoRequirement(itemStack);
            ItemStack[] ammoItems = {
                    SCInventoryItemFinder.getItemFromInventory(player, ammoReq.firstItem(), ammoReq.firstItem2nOption()),
                    SCInventoryItemFinder.getItemFromInventory(player, ammoReq.secondItem(), ammoReq.secondItem2nOption()),
                    SCInventoryItemFinder.getItemFromInventory(player, ammoReq.thirdItem(), ammoReq.thirdItem2nOption())
            };
            for (ItemStack ammoItem : ammoItems) {
                player.getInventory().removeStack(SCInventoryItemFinder.getItemSlot(player, ammoItem), 1);
            }
        }

        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(false, true, false));
        setRechargeTime(player, 0);
        NBTDataHelper.get(itemStack, INBTKeys.RECHARGE, false);
    }

    private static void resetWeaponState(ServerPlayerEntity player, ItemStack itemStack) {
        var state = SCRangeWeaponUtil.getWeaponState(itemStack);
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(false, state.isCharged(), false));
        setRechargeTime(player, 0);
    }

    private static void resetRechargeTime(PlayerEntity player) {
        setRechargeTime(player, 0);
    }

    private static void incrementRechargeTime(PlayerEntity player) {
        setRechargeTime(player, getRechargeTime(player) + 1);
    }

    private static int getRechargeTime(PlayerEntity player) {
        return NBTDataHelper.get((IEntityDataSaver) player, PDKeys.RECHARGE_TIME, 0);
    }

    private static void setRechargeTime(PlayerEntity player, int time) {
        NBTDataHelper.set((IEntityDataSaver) player, PDKeys.RECHARGE_TIME, time);
    }
}