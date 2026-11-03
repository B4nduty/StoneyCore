package banduty.stoneycore.combat.weapon;

import banduty.stoneycore.combat.mechanics.AttackSpeedHelper;
import banduty.stoneycore.combat.range.RangedWeaponHandlers;
import banduty.stoneycore.data.SCDataComponents;
import banduty.stoneycore.definitions.WeaponDefinitionData;
import banduty.stoneycore.definitions.WeaponDefinitionsStorage;
import banduty.stoneycore.entity.projectile.SCArrowEntity;
import banduty.stoneycore.entity.projectile.SCBulletEntity;
import banduty.stoneycore.particle.SCParticles;
import banduty.stoneycore.util.SCInventoryItemFinder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
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
import java.util.concurrent.ConcurrentHashMap;

public final class SCRangeWeaponUtil {
    /** Shot sounds fade out linearly and are inaudible past this many blocks. */
    private static final double SOUND_RANGE = 100.0;
    /** Vanilla only delivers non-forced particles within this many blocks. */
    private static final double PARTICLE_RANGE_SQR = 32.0 * 32.0;
    private static final double PARTICLE_REDUCED_RANGE_SQR = 16.0 * 16.0;

    private SCRangeWeaponUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ---------------------------------------------------------------- state

    public static WeaponState getWeaponState(ItemStack stack) {
        return new WeaponState(
                stack.getOrDefault(SCDataComponents.RELOADING.get(), false),
                stack.getOrDefault(SCDataComponents.CHARGED.get(), false),
                stack.getOrDefault(SCDataComponents.SHOOTING.get(), false)
        );
    }

    public static void setWeaponState(ItemStack stack, WeaponState state) {
        // Called every tick by several handlers; skip the writes (and the resulting item sync) when nothing changed.
        if (getWeaponState(stack).equals(state)) return;
        stack.set(SCDataComponents.RELOADING.get(), state.isReloading());
        stack.set(SCDataComponents.CHARGED.get(), state.isCharged());
        stack.set(SCDataComponents.SHOOTING.get(), state.isShooting());
    }

    // ------------------------------------------------------------- dispatch

    private static WeaponDefinitionData.RangedData rangedData(ItemStack stack) {
        WeaponDefinitionData data = WeaponDefinitionsStorage.getData(stack);
        return data == null ? null : data.ranged();
    }

    public static void handleShoot(Level level, LivingEntity livingEntity, ItemStack weapon) {
        var ranged = rangedData(weapon);
        if (ranged == null) return;
        RangedWeaponHandlers.get(ranged.id()).ifPresent(handler -> {
            if (handler.canShoot(weapon)) handler.shoot(level, livingEntity, weapon);
        });
    }

    public static void handleReload(Level level, LivingEntity livingEntity, ItemStack weapon) {
        var ranged = rangedData(weapon);
        if (ranged == null) return;
        RangedWeaponHandlers.get(ranged.id()).ifPresent(h -> h.reload(level, livingEntity, weapon));
    }

    public static InteractionResultHolder<ItemStack> handleCrossbowUse(Level world, LivingEntity livingEntity, InteractionHand hand, ItemStack stack) {
        if (getWeaponState(stack).isCharged()) {
            handleShoot(world, livingEntity, stack);
        }
        livingEntity.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    // ------------------------------------------------------------- shooting

    public static void shootArrow(Level level, ItemStack stack, LivingEntity livingEntity, ItemStack arrowStack, float pullProgress) {
        if (level == null || level.isClientSide() || livingEntity == null || arrowStack == null || arrowStack.isEmpty())
            return;
        if (!(arrowStack.getItem() instanceof ArrowItem arrowItem)) return;

        var ranged = rangedData(stack);
        if (ranged == null) return;

        AbstractArrow arrowEntity = arrowItem.createArrow(level, arrowStack, livingEntity, stack);
        arrowEntity.setBaseDamage(ranged.baseDamage() / ranged.speed());

        if (arrowEntity instanceof SCArrowEntity scArrowEntity)
            scArrowEntity.setDamageType(ranged.damageType());

        if (Boolean.TRUE.equals(stack.get(SCDataComponents.ARROW_IGNITED.get()))) {
            arrowEntity.igniteForSeconds(5);
        }

        arrowEntity.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot(), 0.0F,
                pullProgress * ranged.speed(), ranged.divergence());

        if (!isCreative(livingEntity)) {
            stack.hurtAndBreak(1, livingEntity, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
        }

        level.addFreshEntity(arrowEntity);
        playShotSound(level, ranged, livingEntity);
    }

    public static void shootBullet(Level level, ItemStack stack, LivingEntity livingEntity) {
        if (!(level instanceof ServerLevel serverLevel) || livingEntity == null) return;

        var ranged = rangedData(stack);
        if (ranged == null) return;

        SCBulletEntity bulletEntity = new SCBulletEntity(level, livingEntity, stack);
        bulletEntity.setDamageAmount(ranged.baseDamage());
        bulletEntity.setDamageType(ranged.damageType());
        bulletEntity.setOwner(livingEntity);
        bulletEntity.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot(), 0.0F,
                ranged.speed(), ranged.divergence());

        level.addFreshEntity(bulletEntity);
        playShotSound(level, ranged, livingEntity);

        // Read the hand before hurtAndBreak, which may break the weapon.
        InteractionHand hand = livingEntity.getUsedItemHand();
        if (!isCreative(livingEntity)) {
            stack.hurtAndBreak(1, livingEntity, LivingEntity.getSlotForHand(hand));
        }

        Vec3 handPos = getHandPosition(livingEntity, hand);
        Vec3 lookDir = livingEntity.getViewVector(1.0F);
        spawnParticleTrail(serverLevel, livingEntity, handPos, lookDir, SCParticles.MUZZLES_SMOKE_PARTICLE.get(), 50, 0.2f, 0.0005f, 5);
        spawnParticleTrail(serverLevel, livingEntity, handPos, lookDir, SCParticles.MUZZLES_FLASH_PARTICLE.get(), 1, 0f, 0.1f, 6);
    }

    private static void playShotSound(Level level, WeaponDefinitionData.RangedData ranged, LivingEntity shooter) {
        if (ranged.soundEvent() == null) return;

        double rangeSqr = SOUND_RANGE * SOUND_RANGE;
        for (Player listener : level.players()) {
            double distanceSqr = shooter.distanceToSqr(listener);
            if (distanceSqr >= rangeSqr) continue;
            float volume = (float) (1.0 - Math.sqrt(distanceSqr) / SOUND_RANGE);
            listener.playNotifySound(ranged.soundEvent(), SoundSource.BLOCKS, volume, 1.0F);
        }
    }

    /**
     * Sends the trail to each nearby player once. Using the broadcasting {@code sendParticles}
     * inside a per-player loop would send every packet to every player N times.
     */
    private static void spawnParticleTrail(ServerLevel serverLevel, LivingEntity shooter, Vec3 handPos, Vec3 lookDir,
                                           ParticleOptions particle, int count, float delta, float spread, int length) {
        for (ServerPlayer viewer : serverLevel.players()) {
            double distanceSqr = viewer.distanceToSqr(shooter);
            if (distanceSqr > PARTICLE_RANGE_SQR) continue;
            int adjustedCount = distanceSqr > PARTICLE_REDUCED_RANGE_SQR ? Math.max(1, count / 2) : count;

            for (int i = 0; i < length; i++) {
                Vec3 pos = handPos.add(lookDir.scale(i));
                serverLevel.sendParticles(viewer, particle, false, pos.x, pos.y, pos.z,
                        adjustedCount, delta, delta, delta, spread);
            }
        }
    }

    private static Vec3 getHandPosition(LivingEntity livingEntity, InteractionHand hand) {
        double xOffset = hand == InteractionHand.MAIN_HAND ? 0.1 : -0.1;
        Vec3 look = livingEntity.getViewVector(1.0F);
        Vec3 side = look.cross(new Vec3(0, 1, 0)).scale(xOffset);
        return livingEntity.position().add(0, 1.5, 0).add(side).add(look.scale(1.5));
    }

    // ---------------------------------------------------------------- arrows

    /** The arrow stack a shot would use: offhand first, then the main inventory. Non-players get a plain arrow. */
    public static Optional<ItemStack> getArrowFromInventory(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return Optional.of(new ItemStack(Items.ARROW));
        ItemStack offhand = livingEntity.getOffhandItem();
        if (offhand.getItem() instanceof ArrowItem) return Optional.of(offhand);

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) return Optional.of(stack);
        }
        return Optional.empty();
    }

    public static float getBowPullProgress(int useTicks) {
        float pull = (float) useTicks / 20.0F;
        pull = (pull * pull + pull * 2.0F) / 3.0F;
        return Math.min(pull, 1.0F);
    }

    public static float getCrossbowPullProgress(int useTicks, ItemStack itemStack, LivingEntity livingEntity) {
        if (rangedData(itemStack) == null) return 0;
        return getCrossbowPullProgress(useTicks, AttackSpeedHelper.getReloadSpeedModified(livingEntity, itemStack));
    }

    /** Same as above with the charge time already resolved, for callers that need several samples per tick. */
    public static float getCrossbowPullProgress(int useTicks, int chargeTime) {
        if (useTicks <= 1) return 0f;
        return Math.min((float) useTicks / Math.max(1, chargeTime), 1.0F);
    }

    // ------------------------------------------------------------------ ammo

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

    private record CachedAmmo(WeaponDefinitionData.RangedData source, AmmoRequirement requirement) {
    }

    /**
     * Parsed requirements per item. Entries remember the definition they came from, so a datapack
     * reload (which creates new definition objects) invalidates them without any explicit hook.
     */
    private static final Map<Item, CachedAmmo> AMMO_CACHE = new ConcurrentHashMap<>();

    public static @NotNull AmmoRequirement getAmmoRequirement(ItemStack itemStack) {
        var ranged = rangedData(itemStack);
        if (ranged == null) return AmmoRequirement.EMPTY;

        Item item = itemStack.getItem();
        CachedAmmo cached = AMMO_CACHE.get(item);
        if (cached != null && cached.source() == ranged) return cached.requirement();

        AmmoRequirement requirement = parseAmmoRequirement(ranged.ammoRequirement());
        AMMO_CACHE.put(item, new CachedAmmo(ranged, requirement));
        return requirement;
    }

    private static AmmoRequirement parseAmmoRequirement(Map<String, WeaponDefinitionData.AmmoRequirementData> map) {
        var first = map.get("item1");
        if (first == null) return AmmoRequirement.EMPTY;
        Item[] firstItems = getItemsFromIds(first.itemIds());
        if (firstItems.length == 0) return AmmoRequirement.EMPTY;

        var second = map.get("item2");
        var third = map.get("item3");
        Item[] secondItems = second != null ? getItemsFromIds(second.itemIds()) : new Item[0];
        Item[] thirdItems = third != null ? getItemsFromIds(third.itemIds()) : new Item[0];

        return new AmmoRequirement(
                first.amount(), firstItems[0], option(firstItems, 1),
                second != null ? second.amount() : 0, option(secondItems, 0), option(secondItems, 1),
                third != null ? third.amount() : 0, option(thirdItems, 0), option(thirdItems, 1)
        );
    }

    private static Item option(Item[] items, int index) {
        return items.length > index ? items[index] : Items.AIR;
    }

    private static Item[] getItemsFromIds(Set<String> itemIds) {
        return itemIds.stream()
                .map(ResourceLocation::parse)
                .map(BuiltInRegistries.ITEM::get)
                .filter(i -> i != Items.AIR)
                .toArray(Item[]::new);
    }

    public static boolean hasRequiredAmmo(ServerPlayer player, AmmoRequirement req) {
        return SCInventoryItemFinder.countItem(player, req.firstItem(), req.firstItem2nOption()) >= req.amountFirstItem()
                && SCInventoryItemFinder.countItem(player, req.secondItem(), req.secondItem2nOption()) >= req.amountSecondItem()
                && SCInventoryItemFinder.countItem(player, req.thirdItem(), req.thirdItem2nOption()) >= req.amountThirdItem();
    }

    public static void consumeAmmo(ServerPlayer player, AmmoRequirement req) {
        SCInventoryItemFinder.consumeItems(player, req.amountFirstItem(), req.firstItem(), req.firstItem2nOption());
        SCInventoryItemFinder.consumeItems(player, req.amountSecondItem(), req.secondItem(), req.secondItem2nOption());
        SCInventoryItemFinder.consumeItems(player, req.amountThirdItem(), req.thirdItem(), req.thirdItem2nOption());
    }

    private static boolean isCreative(LivingEntity entity) {
        return entity instanceof Player player && player.isCreative();
    }

    // ----------------------------------------------------------------- types

    public record WeaponState(boolean isReloading, boolean isCharged, boolean isShooting) {
        public static WeaponState idle() {
            return new WeaponState(false, false, false);
        }

        public static WeaponState reloading() {
            return new WeaponState(true, false, false);
        }

        public static WeaponState charged() {
            return new WeaponState(false, true, false);
        }

        public static WeaponState shooting() {
            return new WeaponState(false, false, true);
        }
    }
}
