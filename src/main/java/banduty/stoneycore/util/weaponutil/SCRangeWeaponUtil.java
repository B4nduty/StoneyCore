package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.combat.range.RangedWeaponHandlers;
import banduty.stoneycore.entity.custom.SCArrowEntity;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public final class SCRangeWeaponUtil {
    private SCRangeWeaponUtil() { throw new UnsupportedOperationException("Utility class"); }

    private static final String KEY_RELOAD = "reload";
    private static final String KEY_CHARGED = "charged";
    private static final String KEY_SHOOT = "shoot";

    public static WeaponState getWeaponState(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null ? WeaponState.fromNbt(nbt) : new WeaponState(false, false, false);
    }

    public static void setWeaponState(ItemStack stack, WeaponState state) {
        NbtCompound nbt = stack.getOrCreateNbt();
        state.applyToNbt(nbt);
    }

    public static void handleShoot(World world, PlayerEntity player, ItemStack weapon) {
        var def = WeaponDefinitionsLoader.getData(weapon);
        if (def == null || def.ranged() == null) return;
        String type = def.ranged().id();
        RangedWeaponHandlers.get(type).ifPresentOrElse(
                handler -> {
                    if (handler.canShoot(weapon)) handler.shoot(world, player, weapon);
                },
                () -> shootBullet(world, weapon, player)
        );
    }

    public static void handleReload(World world, PlayerEntity player, ItemStack weapon) {
        var def = WeaponDefinitionsLoader.getData(weapon);
        if (def == null || def.ranged() == null) return;
        String type = def.ranged().id();
        RangedWeaponHandlers.get(type).ifPresent(h -> h.reload(world, player, weapon));
    }

    public static void shootArrow(World world, ItemStack stack, PlayerEntity player, ItemStack arrowStack, float pullProgress) {
        if (world == null || player == null || arrowStack == null || arrowStack.isEmpty()) return;
        if (!(arrowStack.getItem() instanceof ArrowItem arrowItem)) return;

        PersistentProjectileEntity arrowEntity = arrowItem.createArrow(world, arrowStack, player);
        arrowEntity.setDamage(WeaponDefinitionsLoader.getData(stack).ranged().baseDamage());
        if (arrowEntity instanceof SCArrowEntity scArrowEntity)
            scArrowEntity.setDamageType(WeaponDefinitionsLoader.getData(stack).ranged().damageType());
        arrowEntity.setOwner(player);

        if (NBTDataHelper.get(arrowStack, INBTKeys.IGNITED, false)) {
            arrowEntity.setOnFire(true);
            arrowEntity.setFireTicks(1000);
        }

        arrowEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F,
                pullProgress * WeaponDefinitionsLoader.getData(stack).ranged().speed(),
                WeaponDefinitionsLoader.getData(stack).ranged().divergence());

        if (player.isCreative()) {
            arrowEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        } else {
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
        }

        world.spawnEntity(arrowEntity);
        playSoundForPlayers(world, stack, player);
    }

    public static void shootBullet(World world, ItemStack stack, PlayerEntity player) {
        SCBulletEntity bulletEntity = new SCBulletEntity(player, world);
        bulletEntity.setDamageAmount(WeaponDefinitionsLoader.getData(stack).ranged().baseDamage());
        bulletEntity.setDamageType(WeaponDefinitionsLoader.getData(stack).ranged().damageType());
        bulletEntity.setOwner(player);

        bulletEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F,
                WeaponDefinitionsLoader.getData(stack).ranged().speed(),
                WeaponDefinitionsLoader.getData(stack).ranged().divergence());

        world.spawnEntity(bulletEntity);
        playSoundForPlayers(world, stack, player);

        if (!player.isCreative()) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
        }

        if (world instanceof ServerWorld serverWorld) {
            spawnParticleTrail(serverWorld, player, player.getActiveHand(), ModParticles.MUZZLES_SMOKE_PARTICLE.get(), 100, 0.2f, 0.0005f, 5);
            spawnParticleTrail(serverWorld, player, player.getActiveHand(), ModParticles.MUZZLES_FLASH_PARTICLE.get(), 1, 0f, 0.1f, 6);
        }
    }

    private static void playSoundForPlayers(World world, ItemStack stack, PlayerEntity player) {
        if (world == null) return;
        var definitionData = WeaponDefinitionsLoader.getData(stack);
        if (definitionData == null || definitionData.ranged() == null || definitionData.ranged().soundEvent() == null)
            return;

        for (PlayerEntity playerEntity : world.getPlayers()) {
            if (playerEntity == null) continue;
            Vec3d hearPos = playerEntity.getPos();
            double distance = player.getPos().distanceTo(hearPos);
            float volume = (float) Math.max(0, 1 - (distance * 0.01));
            if (volume > 0) playerEntity.playSound(definitionData.ranged().soundEvent(), SoundCategory.BLOCKS, volume, 1.0F);
        }
    }

    private static void spawnParticleTrail(ServerWorld world, PlayerEntity player, Hand hand, ParticleEffect particle, int count, float delta, float spread, int distance) {
        Vec3d handPos = getHandPosition(player, hand);
        Vec3d lookDir = player.getRotationVec(1.0F);

        for (int i = 0; i < distance; i++) {
            Vec3d pos = handPos.add(lookDir.multiply(i));
            world.spawnParticles(particle, pos.x, pos.y, pos.z, count, delta, delta, delta, spread);
        }
    }

    private static Vec3d getHandPosition(PlayerEntity player, Hand hand) {
        boolean isMainHand = hand == Hand.MAIN_HAND;

        double xOffset = isMainHand ? 0.1 : -0.1;
        double yOffset = 1.5;
        double zOffset = 1.5;

        Vec3d basePos = player.getPos().add(0, yOffset, 0);
        Vec3d sideOffset = player.getRotationVec(1.0F).crossProduct(new Vec3d(0, 1, 0)).multiply(xOffset);

        return basePos.add(sideOffset).add(player.getRotationVec(1.0F).multiply(zOffset));
    }

    public static Optional<ItemStack> getArrowFromInventory(PlayerEntity player) {
        return player.getInventory().main.stream()
                .filter(stack -> !stack.isEmpty() && stack.getItem() instanceof ArrowItem)
                .findFirst();
    }

    public static int getArrowSlot(PlayerEntity player) {
        var main = player.getInventory().main;
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
        if (WeaponDefinitionsLoader.getData(itemStack).ranged() == null) return 0;
        int chargeTime = WeaponDefinitionsLoader.getData(itemStack).ranged().rechargeTime() * 20;
        float progress = (float) useTicks / (float) chargeTime;
        return Math.min(progress, 1.0F);
    }

    public static void loadAndPlayCrossbowSound(World world, ItemStack stack, PlayerEntity player) {
        if (world.isClient) return;

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ITEM_CROSSBOW_LOADING_END,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F / (world.getRandom().nextFloat() * 0.5F + 1.0F)
        );

        setWeaponState(stack, new WeaponState(false, true, false));
    }

    public static TypedActionResult<ItemStack> handleCrossbowUse(World world, PlayerEntity player, Hand hand, ItemStack stack) {
        WeaponState state = getWeaponState(stack);

        if (state.isCharged()) {
            Optional<ItemStack> arrowOpt = getArrowFromInventory(player);
            if (arrowOpt.isPresent()) {
                shootArrow(world, stack, player, arrowOpt.get(), 1.0F);
                setWeaponState(stack, new WeaponState(false, false, true));
                return TypedActionResult.consume(stack);
            } else {
                return TypedActionResult.fail(stack);
            }
        } else {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
    }

    public record AmmoRequirement(
            int amountFirstItem, Item firstItem, Item firstItem2nOption,
            int amountSecondItem, Item secondItem, Item secondItem2nOption,
            int amountThirdItem, Item thirdItem, Item thirdItem2nOption
    ) {}

    public static AmmoRequirement getAmmoRequirement(ItemStack itemStack) {
        WeaponDefinitionsLoader.DefinitionData definitionData = WeaponDefinitionsLoader.getData(itemStack);
        if (definitionData.ranged() == null) return null;
        Map<String, WeaponDefinitionsLoader.AmmoRequirementData> ammoRequirementMap = definitionData.ranged().ammoRequirement();

        int amountFirstItem = 0;
        Item[] firstItems = null;

        int amountSecondItem = 0;
        Item[] secondItems = null;

        int amountThirdItem = 0;
        Item[] thirdItems = null;

        if (ammoRequirementMap.containsKey("item1")) {
            WeaponDefinitionsLoader.AmmoRequirementData item1Data = ammoRequirementMap.get("item1");
            amountFirstItem = item1Data.amount();
            firstItems = getItemsFromIds(item1Data.itemIds());
        }

        if (ammoRequirementMap.containsKey("item2")) {
            WeaponDefinitionsLoader.AmmoRequirementData item2Data = ammoRequirementMap.get("item2");
            amountSecondItem = item2Data.amount();
            secondItems = getItemsFromIds(item2Data.itemIds());
        }

        if (ammoRequirementMap.containsKey("item3")) {
            WeaponDefinitionsLoader.AmmoRequirementData item3Data = ammoRequirementMap.get("item3");
            amountThirdItem = item3Data.amount();
            thirdItems = getItemsFromIds(item3Data.itemIds());
        }

        return amountFirstItem >= 1
                ? new AmmoRequirement(
                amountFirstItem, firstItems[0], firstItems.length > 1 ? firstItems[1] : null,
                amountSecondItem, secondItems != null ? secondItems[0] : null, secondItems != null && secondItems.length > 1 ? secondItems[1] : null,
                amountThirdItem, thirdItems != null ? thirdItems[0] : null, thirdItems != null && thirdItems.length > 1 ? thirdItems[1] : null
        )
                : null;
    }

    private static Item[] getItemsFromIds(Set<String> itemIds) {
        return itemIds.stream()
                .map(Identifier::new)
                .map(Registries.ITEM::get)
                .toArray(Item[]::new);
    }

    public record WeaponState(boolean isReloading, boolean isCharged, boolean isShooting) {

        public static WeaponState fromNbt(NbtCompound nbt) {
                if (nbt == null) return new WeaponState(false, false, false);
                return new WeaponState(nbt.getBoolean(KEY_RELOAD), nbt.getBoolean(KEY_CHARGED), nbt.getBoolean(KEY_SHOOT));
            }

            public void applyToNbt(NbtCompound nbt) {
                if (nbt == null) return;
                nbt.putBoolean(KEY_RELOAD, isReloading);
                nbt.putBoolean(KEY_CHARGED, isCharged);
                nbt.putBoolean(KEY_SHOOT, isShooting);
            }
        }
}
