package banduty.stoneycore.entity.siege;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Engines loaded with one ammo item and a timed reload (mangonel, trebuchet).
 * Flow: load ammo -> reload countdown -> interact to fire -> cooldown -> {@link #launch} fires at the delay mark.
 */
public abstract class AmmoLoadingSiegeEntity extends AbstractSiegeEntity {

    protected static final EntityDataAccessor<String> AMMO_LOADED =
            SynchedEntityData.defineId(AmmoLoadingSiegeEntity.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Integer> RELOAD_TIME =
            SynchedEntityData.defineId(AmmoLoadingSiegeEntity.class, EntityDataSerializers.INT);

    protected AmmoLoadingSiegeEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    /** Accepted ammo and what each costs. */
    protected abstract List<LoadingStage> ammoTypes();

    /** Spawns the projectile. The loaded ammo is cleared afterwards. */
    protected abstract void launch(ServerLevel level, Item ammo);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AMMO_LOADED, "");
        builder.define(RELOAD_TIME, 0);
    }

    // ---- state

    public boolean hasAmmoLoaded() { return !entityData.get(AMMO_LOADED).isEmpty(); }

    public int getReloadTime() { return entityData.get(RELOAD_TIME); }

    public void setReloadTime(int ticks) { entityData.set(RELOAD_TIME, Math.max(0, ticks)); }

    public boolean isReloadComplete() { return getReloadTime() <= 0; }

    /** Ammo is loaded and the reload countdown finished. */
    public boolean isLoaded() { return hasAmmoLoaded() && isReloadComplete(); }

    private void setAmmo(@Nullable Item item) {
        entityData.set(AMMO_LOADED, item == null ? "" : BuiltInRegistries.ITEM.getKey(item).toString());
    }

    public Item getLoadedAmmo() {
        ResourceLocation id = ResourceLocation.tryParse(entityData.get(AMMO_LOADED));
        return id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
    }

    @Override
    protected boolean isReloading() { return getReloadTime() > 0; }

    @Override
    protected void tickTimers() {
        if (getReloadTime() > 0) setReloadTime(getReloadTime() - 1);
    }

    // ---- interaction

    @Override
    protected InteractionResult operate(Player player, ItemStack stack, ServerLevel level) {
        if (getCooldown() > 0) return InteractionResult.FAIL;

        if (!hasAmmoLoaded()) return tryLoad(player, stack);

        if (isReloadComplete()) {
            setOwner(player);
            beginFiring(level);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult tryLoad(Player player, ItemStack stack) {
        LoadingStage match = null;
        for (LoadingStage ammo : ammoTypes()) {
            if (ammo.matches(stack.getItem())) {
                match = ammo;
                break;
            }
        }

        if (match == null) {
            MutableComponent list = Component.empty();
            for (LoadingStage ammo : ammoTypes()) {
                if (!list.getSiblings().isEmpty()) list.append(", ");
                list.append(ammo.item().getDefaultInstance().getHoverName());
            }
            player.displayClientMessage(Component.translatable("siege.loading.need_one_of", list), true);
            return InteractionResult.FAIL;
        }

        if (!player.isCreative() && !match.hasEnough(stack)) {
            player.displayClientMessage(Component.translatable("siege.loading.need_amount", match.amount(),
                    match.item().getDefaultInstance().getHoverName()), true);
            return InteractionResult.FAIL;
        }

        match.apply(player, stack);
        setAmmo(match.item());
        setReloadTime(getBaseReload());
        return InteractionResult.SUCCESS;
    }

    protected void beginFiring(ServerLevel level) {
        playAnimation(SiegeAnimation.FIRE);
        startCooldown();
        playShootSound(level);
    }

    @Override
    protected void onSiegeTick(ServerLevel level) {
        if (hasAmmoLoaded() && getCooldown() == getProperties().fireTriggerCooldown() && getCooldown() > 0) {
            launch(level, getLoadedAmmo());
            setAmmo(null);
        }
    }

    // ---- persistence

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Ammo", entityData.get(AMMO_LOADED));
        tag.putInt("ReloadTime", getReloadTime());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(AMMO_LOADED, tag.getString("Ammo"));
        setReloadTime(tag.getInt("ReloadTime"));
    }
}
