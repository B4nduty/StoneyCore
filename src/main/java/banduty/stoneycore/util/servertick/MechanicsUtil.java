package banduty.stoneycore.util.servertick;

import banduty.stoneycore.util.SCInventoryItemFinder;
import banduty.stoneycore.util.definitionsloader.SCWeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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
        ItemStack activeItem = player.getActiveItem();
        boolean isBlocking = player.isBlocking();
        boolean usingCustomShield = activeItem.isIn(SCTags.WEAPONS_SHIELD.getTag());

        NbtCompound persistentData = ((IEntityDataSaver) player).stoneycore$getPersistentData();

        if (isBlocking && usingCustomShield) {
            if (!persistentData.contains("BlockStartTick")) {
                persistentData.putInt("BlockStartTick", (int) player.getWorld().getTime());
            }
        } else {
            persistentData.remove("BlockStartTick");
        }
    }

    public static void handlePlayerReload(ServerPlayerEntity player) {
        ItemStack currentItem = player.getMainHandStack();
        Item currentItemType = currentItem.getItem();
        NbtCompound nbt = currentItem.getNbt();

        var weaponState = SCRangeWeaponUtil.getWeaponState(currentItem);

        if (weaponState.isReloading()) {
            player.setVelocity(0, player.getVelocity().y, 0);
            player.velocityDirty = true;
        }

        ItemStack lastItem = LAST_ITEMSTACK_MAP.get(player);
        if (currentItem != lastItem) {
            if (lastItem != null && lastItem.getNbt() != null && SCWeaponDefinitionsLoader.isRanged(lastItem)) {
                lastItem.getNbt().putBoolean("sc_recharge", false);
                resetWeaponState(player, lastItem);
            }
            LAST_ITEMSTACK_MAP.put(player, currentItem);
            return;
        }

        if (nbt == null || !isReloading(nbt) || !isValidRangeWeapon(currentItemType)) {
            resetRechargeTime(player);
            return;
        }

        if (player.isCreative()) {
            completeReload(player, currentItem);
            return;
        }

        if (player.getItemCooldownManager().isCoolingDown(currentItemType) || weaponState.isCharged()) return;

        if (weaponState.isReloading()) {
            incrementRechargeTime(player);
        } else if (hasRequiredAmmo(player, currentItemType)) {
            startReload(player, currentItem);
        } else {
            currentItem.getOrCreateNbt().putBoolean("sc_recharge", false);
        }

        int requiredTicks = SCWeaponDefinitionsLoader.getData(currentItemType).ranged().rechargeTime() * 20;
        if (getRechargeTime(player) >= requiredTicks) {
            completeReload(player, currentItem);
        }
    }

    private static boolean isReloading(NbtCompound nbt) {
        return nbt.getBoolean("sc_recharge");
    }

    private static boolean isValidRangeWeapon(Item item) {
        return SCWeaponDefinitionsLoader.isRanged(item);
    }

    private static boolean hasRequiredAmmo(ServerPlayerEntity player, Item item) {
        var ammoReq = SCRangeWeaponUtil.getAmmoRequirement(item);
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
            var ammoReq = SCRangeWeaponUtil.getAmmoRequirement(itemStack.getItem());
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
        itemStack.getOrCreateNbt().putBoolean("sc_recharge", false);
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
        return ((IEntityDataSaver) player).stoneycore$getPersistentData().getInt("rechargeTime");
    }

    private static void setRechargeTime(PlayerEntity player, int time) {
        ((IEntityDataSaver) player).stoneycore$getPersistentData().putInt("rechargeTime", time);
    }
}