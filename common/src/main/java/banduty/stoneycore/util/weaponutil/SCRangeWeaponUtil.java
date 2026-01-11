package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.combat.range.RangedWeaponHandlers;
import banduty.stoneycore.entity.custom.SCArrowEntity;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SCRangeWeaponUtil {
    private SCRangeWeaponUtil() { throw new UnsupportedOperationException("Utility class"); }

    private static final String KEY_RELOAD = "reload";
    private static final String KEY_CHARGED = "charged";
    private static final String KEY_SHOOT = "shoot";

    public static WeaponState getWeaponState(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? WeaponState.fromNbt(tag) : new WeaponState(false, false, false);
    }

    public static void setWeaponState(ItemStack stack, WeaponState state) {
        CompoundTag tag = stack.getOrCreateTag();
        state.applyToNbt(tag);
    }

    public static void handleShoot(Level level, Player player, ItemStack weapon) {
        var def = WeaponDefinitionsStorage.getData(weapon);
        if (def == null || def.ranged() == null) return;
        String type = def.ranged().id();
        RangedWeaponHandlers.get(type).ifPresentOrElse(
                handler -> {
                    if (handler.canShoot(weapon)) handler.shoot(level, player, weapon);
                },
                () -> shootBullet(level, weapon, player)
        );
    }

    public static void handleReload(Level level, Player player, ItemStack weapon) {
        var def = WeaponDefinitionsStorage.getData(weapon);
        if (def == null || def.ranged() == null) return;
        String type = def.ranged().id();
        RangedWeaponHandlers.get(type).ifPresent(h -> h.reload(level, player, weapon));
    }

    public static void shootArrow(Level level, ItemStack stack, Player player, ItemStack arrowStack, float pullProgress) {
        if (level == null || level.isClientSide() || player == null || arrowStack == null || arrowStack.isEmpty()) return;
        if (!(arrowStack.getItem() instanceof ArrowItem arrowItem)) return;
        NBTDataHelper.set(arrowStack, INBTKeys.FROM_RANGED_WEAPON, true);

        AbstractArrow arrowEntity = arrowItem.createArrow(level, arrowStack, player);
        arrowEntity.setBaseDamage(WeaponDefinitionsStorage.getData(stack).ranged().baseDamage() / WeaponDefinitionsStorage.getData(stack).ranged().speed());
        if (arrowEntity instanceof SCArrowEntity scArrowEntity)
            scArrowEntity.setDamageType(WeaponDefinitionsStorage.getData(stack).ranged().damageType());
        arrowEntity.setOwner(player);

        if (NBTDataHelper.get(arrowStack, INBTKeys.IGNITED, false)) {
            arrowEntity.setSharedFlagOnFire(true);
            arrowEntity.setRemainingFireTicks(1000);
        }

        arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                pullProgress * WeaponDefinitionsStorage.getData(stack).ranged().speed(),
                WeaponDefinitionsStorage.getData(stack).ranged().divergence());

        if (player.isCreative()) {
            arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }

        level.addFreshEntity(arrowEntity);
        playSoundForPlayers(level, stack, player);
    }

    public static void shootBullet(Level level, ItemStack stack, Player player) {
        SCBulletEntity bulletEntity = new SCBulletEntity(player, level);
        bulletEntity.setDamageAmount(WeaponDefinitionsStorage.getData(stack).ranged().baseDamage());
        bulletEntity.setDamageType(WeaponDefinitionsStorage.getData(stack).ranged().damageType());
        bulletEntity.setOwner(player);

        bulletEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                WeaponDefinitionsStorage.getData(stack).ranged().speed(),
                WeaponDefinitionsStorage.getData(stack).ranged().divergence());

        level.addFreshEntity(bulletEntity);
        playSoundForPlayers(level, stack, player);

        if (!player.isCreative()) {
            stack.hurt(1, player.getRandom(), player instanceof ServerPlayer ? (ServerPlayer) player : null);
        }

        if (level instanceof ServerLevel serverLevel) {
            spawnParticleTrail(serverLevel, player, player.getUsedItemHand(), Services.PARTICLE.getMuzzlesSmokeParticle(), 100, 0.2f, 0.0005f, 5);
            spawnParticleTrail(serverLevel, player, player.getUsedItemHand(), Services.PARTICLE.getMuzzlesFlashParticle(), 1, 0f, 0.1f, 6);
        }
    }

    private static void playSoundForPlayers(Level level, ItemStack stack, Player player) {
        if (level == null) return;
        var definitionData = WeaponDefinitionsStorage.getData(stack);
        if (definitionData == null || definitionData.ranged() == null || definitionData.ranged().soundEvent() == null)
            return;

        for (Player playerEntity : level.players()) {
            if (playerEntity == null) continue;
            Vec3 hearPos = playerEntity.position();
            double distance = player.position().distanceTo(hearPos);
            float volume = (float) Math.max(0, 1 - (distance * 0.01));
            if (volume > 0) playerEntity.playNotifySound(definitionData.ranged().soundEvent(), SoundSource.BLOCKS, volume, 1.0F);
        }
    }

    private static void spawnParticleTrail(ServerLevel serverLevel, Player player, InteractionHand hand, ParticleOptions particle, int count, float delta, float spread, int distance) {
        Vec3 handPos = getHandPosition(player, hand);
        Vec3 lookDir = player.getViewVector(1.0F);

        for (int i = 0; i < distance; i++) {
            Vec3 pos = handPos.add(lookDir.scale(i));
            serverLevel.sendParticles(particle, pos.x, pos.y, pos.z, count, delta, delta, delta, spread);
        }
    }

    private static Vec3 getHandPosition(Player player, InteractionHand hand) {
        boolean isMainHand = hand == InteractionHand.MAIN_HAND;

        double xOffset = isMainHand ? 0.1 : -0.1;
        double yOffset = 1.5;
        double zOffset = 1.5;

        Vec3 basePos = player.position().add(0, yOffset, 0);
        Vec3 sideOffset = player.getViewVector(1.0F).cross(new Vec3(0, 1, 0)).scale(xOffset);

        return basePos.add(sideOffset).add(player.getViewVector(1.0F).scale(zOffset));
    }

    public static Optional<ItemStack> getArrowFromInventory(Player player) {
        return player.getInventory().items.stream()
                .filter(stack -> !stack.isEmpty() && stack.getItem() instanceof ArrowItem)
                .findFirst();
    }

    public static int getArrowSlot(Player player) {
        var main = player.getInventory().items;
        for (int i = 0; i < main.size(); i++) {
            ItemStack s = main.get(i);
            if (!s.isEmpty() && s.getItem() instanceof ArrowItem) return i;
        }
        return -1;
    }

    public static float getBowPullProgress(int useTicks) {
        float pull = (float) useTicks / 20.0F;
        pull = (pull * pull + pull * 2.0F) / 3.0F;
        return Math.min(pull, 1.0F);
    }

    public static float getCrossbowPullProgress(int useTicks, ItemStack itemStack) {
        if (WeaponDefinitionsStorage.getData(itemStack).ranged() == null) return 0;
        int chargeTime = WeaponDefinitionsStorage.getData(itemStack).ranged().rechargeTime() * 20;
        float progress = (float) useTicks / (float) chargeTime;
        return Math.min(progress, 1.0F);
    }

    public static void loadAndPlayCrossbowSound(Level level, ItemStack stack, Player player) {
        if (level.isClientSide()) return;

        level.playSound(
                null,
                player.getOnPos(),
                SoundEvents.CROSSBOW_LOADING_END,
                SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F)
        );

        setWeaponState(stack, new WeaponState(false, true, false));
    }

    public static InteractionResultHolder<ItemStack> handleCrossbowUse(Level world, Player player, InteractionHand hand, ItemStack stack) {
        WeaponState state = getWeaponState(stack);

        if (state.isCharged()) {
            Optional<ItemStack> arrowOpt = getArrowFromInventory(player);
            if (arrowOpt.isPresent()) {
                shootArrow(world, stack, player, arrowOpt.get(), 1.0F);
                setWeaponState(stack, new WeaponState(false, false, true));
                return InteractionResultHolder.consume(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    public record AmmoRequirement(
            int amountFirstItem, Item firstItem, Item firstItem2nOption,
            int amountSecondItem, Item secondItem, Item secondItem2nOption,
            int amountThirdItem, Item thirdItem, Item thirdItem2nOption
    ) {
        public static final AmmoRequirement EMPTY = new AmmoRequirement(
                0, Items.AIR, Items.AIR,
                0, Items.AIR, Items.AIR,
                0, Items.AIR, Items.AIR
        );
    }

    public static @NotNull AmmoRequirement getAmmoRequirement(ItemStack itemStack) {
        WeaponDefinitionData definitionData = WeaponDefinitionsStorage.getData(itemStack);
        if (definitionData.ranged() == null) return AmmoRequirement.EMPTY;
        Map<String, WeaponDefinitionData.AmmoRequirementData> ammoRequirementMap = definitionData.ranged().ammoRequirement();

        int amountFirstItem = 0;
        Item[] firstItems = null;

        int amountSecondItem = 0;
        Item[] secondItems = null;

        int amountThirdItem = 0;
        Item[] thirdItems = null;

        if (ammoRequirementMap.containsKey("item1")) {
            WeaponDefinitionData.AmmoRequirementData item1Data = ammoRequirementMap.get("item1");
            amountFirstItem = item1Data.amount();
            firstItems = getItemsFromIds(item1Data.itemIds());
        }

        if (ammoRequirementMap.containsKey("item2")) {
            WeaponDefinitionData.AmmoRequirementData item2Data = ammoRequirementMap.get("item2");
            amountSecondItem = item2Data.amount();
            secondItems = getItemsFromIds(item2Data.itemIds());
        }

        if (ammoRequirementMap.containsKey("item3")) {
            WeaponDefinitionData.AmmoRequirementData item3Data = ammoRequirementMap.get("item3");
            amountThirdItem = item3Data.amount();
            thirdItems = getItemsFromIds(item3Data.itemIds());
        }

        return amountFirstItem >= 1
                ? new AmmoRequirement(
                amountFirstItem, firstItems[0], firstItems.length > 1 ? firstItems[1] : Items.AIR,
                amountSecondItem, secondItems != null ? secondItems[0] : Items.AIR, secondItems != null && secondItems.length > 1 ? secondItems[1] : Items.AIR,
                amountThirdItem, thirdItems != null ? thirdItems[0] : Items.AIR, thirdItems != null && thirdItems.length > 1 ? thirdItems[1] : Items.AIR
        )
                : AmmoRequirement.EMPTY;
    }

    private static Item[] getItemsFromIds(Set<String> itemIds) {
        return itemIds.stream()
                .map(ResourceLocation::new)
                .map(BuiltInRegistries.ITEM::get)
                .toArray(Item[]::new);
    }

    public record WeaponState(boolean isReloading, boolean isCharged, boolean isShooting) {

        public static WeaponState fromNbt(CompoundTag tag) {
                if (tag == null) return new WeaponState(false, false, false);
                return new WeaponState(tag.getBoolean(KEY_RELOAD), tag.getBoolean(KEY_CHARGED), tag.getBoolean(KEY_SHOOT));
            }

            public void applyToNbt(CompoundTag tag) {
                if (tag == null) return;
                tag.putBoolean(KEY_RELOAD, isReloading);
                tag.putBoolean(KEY_CHARGED, isCharged);
                tag.putBoolean(KEY_SHOOT, isShooting);
            }
        }
}
