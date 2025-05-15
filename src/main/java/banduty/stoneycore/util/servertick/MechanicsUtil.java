package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCInventoryItemFinder;
import banduty.stoneycore.util.definitionsloader.SCRangedWeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class MechanicsUtil {
    @Unique
    private static final Map<LivingEntity, ItemStack> LAST_ITEMSTACK_MAP = Collections.synchronizedMap(new WeakHashMap<>());

    public static void handleParry(ServerPlayerEntity player) {
        boolean isBlocking = player.isBlocking();
        ItemStack activeItem = player.getActiveItem();
        boolean usingCustomShield = activeItem.isIn(SCTags.WEAPONS_SHIELD.getTag());

        NbtCompound persistentData = ((IEntityDataSaver) player).stoneycore$getPersistentData();
        boolean wasBlocking = persistentData.contains("BlockStartTick");

        if (isBlocking && usingCustomShield) {
            if (!wasBlocking) {
                persistentData.putInt("BlockStartTick", (int) player.getWorld().getTime());
            }
        } else {
            persistentData.remove("BlockStartTick");
        }
    }

    public static void handlePlayerReload(ServerPlayerEntity player) {
        ItemStack currentItem = player.getMainHandStack();

        if (SCRangeWeaponUtil.getWeaponState(player.getMainHandStack()).isReloading() && StoneyCore.getConfig().getRealisticCombat()) {
            player.setVelocity(0, player.getVelocity().y, 0);
            player.velocityDirty = true;
        }

        ItemStack lastItemStack = LAST_ITEMSTACK_MAP.get(player);

        if (currentItem != lastItemStack) {
            if (lastItemStack != null) {
                if (SCRangedWeaponDefinitionsLoader.containsItem(lastItemStack.getItem())) {
                    lastItemStack.getOrCreateNbt().putBoolean("sc_recharge", false);
                    resetWeaponState(player, lastItemStack);
                }
            }
            LAST_ITEMSTACK_MAP.put(player, currentItem);
            return;
        }

        if (currentItem.getNbt() == null) return;

        Item item = currentItem.getItem();
        if (!isReloading(currentItem) || !isValidRangeWeapon(item)) {
            resetRechargeTime(player);
            return;
        }

        if (player.isCreative()) {
            completeReload(player, currentItem);
            return;
        }

        if (player.getItemCooldownManager().isCoolingDown(item)) {
            return;
        }

        if (SCRangeWeaponUtil.getWeaponState(currentItem).isCharged()) {
            return;
        }

        if (SCRangeWeaponUtil.getWeaponState(currentItem).isReloading()) {
            incrementRechargeTime(player);
        } else if (hasRequiredAmmo(player, item)) {
            startReload(player, currentItem);
        } else {
            currentItem.getOrCreateNbt().putBoolean("sc_recharge", false);
        }

        if (getRechargeTime(player) >= SCRangedWeaponDefinitionsLoader.getData(item).rechargeTime() * 20) {
            completeReload(player, currentItem);
        }
    }

    private static void resetWeaponState(ServerPlayerEntity player, ItemStack itemStack) {
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(false, SCRangeWeaponUtil.getWeaponState(itemStack).isCharged(), false));
        setRechargeTime(player, 0);
    }

    private static boolean isReloading(ItemStack itemStack) {
        return itemStack.getOrCreateNbt().getBoolean("sc_recharge");
    }

    private static boolean isValidRangeWeapon(Item item) {
        return SCRangedWeaponDefinitionsLoader.containsItem(item);
    }

    private static boolean hasRequiredAmmo(ServerPlayerEntity player, Item item) {
        SCRangeWeaponUtil.AmmoRequirement ammoRequirement = SCRangeWeaponUtil.getAmmoRequirement(item);
        ItemStack[] requiredItems = {
                SCInventoryItemFinder.getItemFromInventory(player, ammoRequirement.firstItem(), ammoRequirement.firstItem2nOption()),
                SCInventoryItemFinder.getItemFromInventory(player, ammoRequirement.secondItem(), ammoRequirement.secondItem2nOption()),
                SCInventoryItemFinder.getItemFromInventory(player, ammoRequirement.thirdItem(), ammoRequirement.thirdItem2nOption())
        };

        int[] requiredAmounts = {
                ammoRequirement.amountFirstItem(),
                ammoRequirement.amountSecondItem(),
                ammoRequirement.amountThirdItem()
        };

        return SCInventoryItemFinder.areItemsInInventory(requiredItems, requiredAmounts);
    }

    private static void startReload(ServerPlayerEntity player, ItemStack itemStack) {
        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(true, SCRangeWeaponUtil.getWeaponState(itemStack).isCharged(), false));
        incrementRechargeTime(player);
    }

    private static void completeReload(ServerPlayerEntity player, ItemStack itemStack) {
        SCRangeWeaponUtil.AmmoRequirement ammoRequirement = SCRangeWeaponUtil.getAmmoRequirement(itemStack.getItem());
        if (!player.isCreative()) {
            ItemStack[] ammoItems = {
                    SCInventoryItemFinder.getItemFromInventory(player, ammoRequirement.firstItem(), ammoRequirement.firstItem2nOption()),
                    SCInventoryItemFinder.getItemFromInventory(player, ammoRequirement.secondItem(), ammoRequirement.secondItem2nOption()),
                    SCInventoryItemFinder.getItemFromInventory(player, ammoRequirement.thirdItem(), ammoRequirement.thirdItem2nOption())
            };

            for (ItemStack ammoItem : ammoItems) {
                player.getInventory().removeStack(SCInventoryItemFinder.getItemSlot(player, ammoItem), 1);
            }
        }

        SCRangeWeaponUtil.setWeaponState(itemStack, new SCRangeWeaponUtil.WeaponState(false, true, false));
        setRechargeTime(player, 0);
        itemStack.getOrCreateNbt().putBoolean("sc_recharge", false);
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
