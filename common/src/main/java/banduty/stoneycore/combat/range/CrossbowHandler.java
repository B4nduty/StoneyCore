package banduty.stoneycore.combat.range;

import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, new ItemStack(Items.CROSSBOW));
            serverPlayer.awardStat(Stats.ITEM_USED.get(weapon.getItem()));
        }
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
        if (level.isClientSide()) return;
        if (useTime <= 1) return;

        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack);
        float prevProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime - 1, stack);

        boolean hasAmmo = SCRangeWeaponUtil.getArrowFromInventory(player).isPresent();
        if (player.isCreative()) hasAmmo = true;

        SCRangeWeaponUtil.WeaponState currentState = SCRangeWeaponUtil.getWeaponState(stack);
        SCRangeWeaponUtil.WeaponState nextState = currentState;

        if (pullProgress < 1.0F) {
            if (currentState.isCharged()) {
                shoot(level, player, stack);
                nextState = SCRangeWeaponUtil.WeaponState.shooting();
            } else if (hasAmmo) {
                nextState = SCRangeWeaponUtil.WeaponState.reloading();
            } else {
                nextState = SCRangeWeaponUtil.WeaponState.idle();
            }
        }

        if (pullProgress >= 0.2F && prevProgress < 0.2F) {
            level.playSound(null,
                    player.getOnPos(),
                    net.minecraft.sounds.SoundEvents.CROSSBOW_LOADING_START,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.5F, 1.0F);
        }

        if (pullProgress >= 0.5F && prevProgress < 0.5F) {
            level.playSound(null,
                    player.getOnPos(),
                    net.minecraft.sounds.SoundEvents.CROSSBOW_LOADING_MIDDLE,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.5F, 1.0F);
        }

        if (pullProgress >= 1.0F) {
            if (!currentState.isCharged() && hasAmmo) {
                ItemStack ammoStack = SCRangeWeaponUtil
                        .getArrowFromInventory(player)
                        .orElseGet(() -> new ItemStack(net.minecraft.world.item.Items.ARROW));

                if (!player.isCreative()) {
                    int slot = SCRangeWeaponUtil.getArrowSlot(player);
                    if (slot >= 0) player.getInventory().removeItem(slot, 1);
                }

                CompoundTag nbt = stack.getOrCreateTag();
                nbt.putString("loaded_arrow", BuiltInRegistries.ITEM.getKey(ammoStack.getItem()).toString());

                level.playSound(
                        null,
                        player.getOnPos(),
                        SoundEvents.CROSSBOW_LOADING_END,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F)
                );

                nextState = SCRangeWeaponUtil.WeaponState.charged();
            }
        }

        SCRangeWeaponUtil.setWeaponState(stack, nextState);
    }
}