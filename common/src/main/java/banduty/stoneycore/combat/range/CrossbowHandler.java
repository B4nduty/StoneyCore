package banduty.stoneycore.combat.range;

import banduty.stoneycore.combat.mechanics.AttackSpeedHelper;
import banduty.stoneycore.combat.weapon.SCRangeWeaponUtil;
import banduty.stoneycore.combat.weapon.SCRangeWeaponUtil.WeaponState;
import banduty.stoneycore.data.SCDataComponents;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CrossbowHandler implements IRangedWeaponHandler {
    @Override
    public String getTypeId() {
        return "crossbow";
    }

    @Override
    public void shoot(Level level, LivingEntity livingEntity, ItemStack weapon) {
        if (level == null || livingEntity == null || weapon == null) return;
        if (!SCRangeWeaponUtil.getWeaponState(weapon).isCharged()) return;

        ResourceLocation loaded = weapon.get(SCDataComponents.LOADED_ARROW.get());
        if (loaded == null) return;
        if (!(BuiltInRegistries.ITEM.get(loaded) instanceof ArrowItem arrow)) return;

        SCRangeWeaponUtil.shootArrow(level, weapon, livingEntity, new ItemStack(arrow), 1.0f);
        SCRangeWeaponUtil.setWeaponState(weapon, WeaponState.idle());
        weapon.remove(SCDataComponents.LOADED_ARROW.get());

        if (livingEntity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, new ItemStack(Items.CROSSBOW));
            serverPlayer.awardStat(Stats.ITEM_USED.get(weapon.getItem()));
        }
    }

    @Override
    public void reload(Level level, LivingEntity livingEntity, ItemStack weapon) { /* loaded by holding use */ }

    @Override
    public void handleRelease(ItemStack stack, Level level, LivingEntity livingEntity, int useTime, ItemStack arrowStack) {
        if (SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack, livingEntity) >= 1.0F) return;
        WeaponState state = SCRangeWeaponUtil.getWeaponState(stack);
        SCRangeWeaponUtil.setWeaponState(stack, new WeaponState(false, state.isCharged(), state.isShooting()));
    }

    @Override
    public boolean canShoot(ItemStack weapon) {
        return true;
    }

    @Override
    public void handleUsageTick(Level level, ItemStack stack, LivingEntity livingEntity, int useTime) {
        if (level.isClientSide() || useTime <= 1) return;

        int chargeTime = AttackSpeedHelper.getReloadSpeedModified(livingEntity, stack);
        float progress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, chargeTime);
        float prevProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime - 1, chargeTime);

        boolean creative = livingEntity instanceof Player player && player.isCreative();
        Optional<ItemStack> arrow = SCRangeWeaponUtil.getArrowFromInventory(livingEntity);
        boolean hasAmmo = creative || arrow.isPresent();

        WeaponState state = SCRangeWeaponUtil.getWeaponState(stack);
        WeaponState next = state;

        if (progress < 1.0F) {
            if (state.isCharged()) {
                shoot(level, livingEntity, stack);
                next = WeaponState.shooting();
            } else {
                next = hasAmmo ? WeaponState.reloading() : WeaponState.idle();
            }
        }

        if (crossed(prevProgress, progress, 0.2F)) playLoadingSound(level, livingEntity, SoundEvents.CROSSBOW_LOADING_START.value(), 0.5F, 1.0F);
        if (crossed(prevProgress, progress, 0.5F)) playLoadingSound(level, livingEntity, SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), 0.5F, 1.0F);

        if (progress >= 1.0F && !state.isCharged() && hasAmmo) {
            ItemStack ammo = arrow.orElseGet(() -> new ItemStack(Items.ARROW));
            stack.set(SCDataComponents.LOADED_ARROW.get(), BuiltInRegistries.ITEM.getKey(ammo.getItem()));
            // Shrink the exact stack the arrow type came from (it may be the offhand).
            if (!creative && livingEntity instanceof Player) ammo.shrink(1);

            playLoadingSound(level, livingEntity, SoundEvents.CROSSBOW_LOADING_END.value(), 1.0F,
                    1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F));
            next = WeaponState.charged();
        }

        SCRangeWeaponUtil.setWeaponState(stack, next);
    }

    private static boolean crossed(float prev, float now, float threshold) {
        return now >= threshold && prev < threshold;
    }

    private static void playLoadingSound(Level level, LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}
