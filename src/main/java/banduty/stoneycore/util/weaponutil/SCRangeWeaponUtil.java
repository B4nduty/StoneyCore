package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.entity.custom.SCArrowEntity;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import banduty.stoneycore.items.item.SCArrow;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
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
    private SCRangeWeaponUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static TypedActionResult<ItemStack> handleCrossbowUse(World world, PlayerEntity user, Hand hand, ItemStack itemStack) {
        if (world == null || user == null || itemStack == null) {
            return TypedActionResult.fail(ItemStack.EMPTY);
        }

        Optional<ItemStack> arrowStackOpt = getArrowFromInventory(user);
        if (arrowStackOpt.isPresent() && arrowStackOpt.get().getItem() instanceof SCArrow scArrow) {
            SCArrowEntity arrowEntity = (SCArrowEntity) scArrow.createArrowEntity(user, world);
            WeaponState weaponState = getWeaponState(itemStack);
            NbtCompound nbt = itemStack.getOrCreateNbt();
            Projectiles projectiles = Projectiles.fromNbt(nbt, arrowEntity);

            if (weaponState.isCharged()) {
                setWeaponState(itemStack, new WeaponState(weaponState.isReloading(), false, true));
                projectiles = projectiles.unloadProjectile();
                projectiles.applyToNbt(nbt);
                shootArrow(world, itemStack, user, scArrow.getDefaultStack(), 1f);
                return TypedActionResult.consume(itemStack);
            } else if (projectiles.getArrowCount() < 1) {
                setWeaponState(itemStack, new WeaponState(true, false, false));
                user.setCurrentHand(hand);
                return TypedActionResult.consume(itemStack);
            } else {
                return TypedActionResult.fail(itemStack);
            }
        }
        return TypedActionResult.fail(itemStack);
    }

    public static void shootArrow(World world, ItemStack stack, PlayerEntity player, ItemStack arrowStack, float pullProgress) {
        SCArrowEntity arrowEntity = (SCArrowEntity) ((SCArrow) arrowStack.getItem()).createArrowEntity(player, world);
        arrowEntity.setDamageAmount(WeaponDefinitionsLoader.getData(stack).ranged().baseDamage());
        arrowEntity.setDamageType(WeaponDefinitionsLoader.getData(stack).ranged().damageType());
        arrowEntity.setOwner(player);
        if (arrowStack.getNbt() != null && arrowStack.getNbt().getBoolean("ignited")) {
            arrowEntity.setOnFire(true);
            arrowEntity.setFireTicks(1000);
        }

        arrowEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, pullProgress * WeaponDefinitionsLoader.getData(stack).ranged().speed(), WeaponDefinitionsLoader.getData(stack).ranged().divergence());

        if (player.isCreative()) {
            arrowEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        } else {
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
            player.getInventory().removeStack(getArrowSlot(player), 1);
        }

        world.spawnEntity(arrowEntity);
        playSoundForPlayers(world, stack, player);
    }

    public static void shootBullet(World world, ItemStack stack, PlayerEntity player) {
        SCBulletEntity bulletEntity = new SCBulletEntity(player, world);
        bulletEntity.setDamageAmount(WeaponDefinitionsLoader.getData(stack).ranged().baseDamage());
        bulletEntity.setDamageType(WeaponDefinitionsLoader.getData(stack).ranged().damageType());
        bulletEntity.setOwner(player);

        bulletEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, WeaponDefinitionsLoader.getData(stack).ranged().speed(), WeaponDefinitionsLoader.getData(stack).ranged().divergence());

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
        for (PlayerEntity playerEntity : world.getPlayers()) {
            if (playerEntity != null) {
                Vec3d playerPos = player.getPos();
                Vec3d hearPos = playerEntity.getPos();
                double distance = playerPos.distanceTo(hearPos);
                float volume = (float) Math.max(0, 1 - (distance * 0.01));
                if (volume != 0) {
                    WeaponDefinitionsLoader.DefinitionData definitionData = WeaponDefinitionsLoader.getData(stack);
                    if (definitionData.ranged().soundEvent() != null) player.playSound(definitionData.ranged().soundEvent(), SoundCategory.BLOCKS, volume, 1.0F);
                }
            }
        }
    }

    private static void spawnParticleTrail(ServerWorld world, PlayerEntity player, Hand hand, ParticleEffect particle, int count, float delta, float spread, int distance) {
        Vec3d handPos = getHandPosition(player, hand);
        Vec3d lookDir = player.getRotationVec(1.0F);

        List<Vec3d> trailPositions = new ArrayList<>();
        for (int i = 0; i < distance; i++) {
            trailPositions.add(handPos.add(lookDir.multiply(i)));
        }

        for (Vec3d pos : trailPositions) {
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

    public static void loadAndPlayCrossbowSound(World world, ItemStack stack, PlayerEntity player, ItemStack arrowStack) {
        SCArrowEntity arrowEntity = (SCArrowEntity) ((SCArrow) arrowStack.getItem()).createArrowEntity(player, world);
        Projectiles.fromNbt(stack.getOrCreateNbt(), arrowEntity).loadProjectile();
        setWeaponState(stack, new WeaponState(false, true, getWeaponState(stack).isShooting()));

        SoundCategory soundCategory = player instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_CROSSBOW_LOADING_END, soundCategory, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        if (!player.getAbilities().creativeMode) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
        }
    }

    public static Optional<ItemStack> getArrowFromInventory(PlayerEntity player) {
        return player.getInventory().main.stream()
                .filter(stack -> stack.getItem() instanceof SCArrow)
                .findFirst();
    }

    private static int getArrowSlot(PlayerEntity player) {
        return player.getInventory().main.stream()
                .filter(stack -> stack.getItem() instanceof SCArrow)
                .map(player.getInventory().main::indexOf)
                .findFirst().orElse(-1);
    }

    public static float getBowPullProgress(int useTicks) {
        float progress = useTicks / 20.0F;
        return Math.min((progress * progress + progress * 2.0F) / 3.0F, 1.0F);
    }

    public static float getCrossbowPullProgress(int useTicks, Item item) {
        return Math.min((float) useTicks / (WeaponDefinitionsLoader.getData(item).ranged().rechargeTime() * 20), 1.0F);
    }

    public static WeaponState getWeaponState(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null ? WeaponState.fromNbt(nbt) : new WeaponState(false, false, false);
    }

    public static void setWeaponState(ItemStack stack, WeaponState state) {
        NbtCompound nbt = stack.getOrCreateNbt();
        state.applyToNbt(nbt);
    }

    public record Projectiles(SCArrowEntity scArrowEntity, int arrowCount) {

        public static Projectiles fromNbt(NbtCompound nbt, SCArrowEntity scArrowEntity) {
            int count = nbt.contains(scArrowEntity.getEntityName()) ? nbt.getInt(scArrowEntity.getEntityName()) : 0;
            return new Projectiles(scArrowEntity, count);
        }

        public void loadProjectile() {
            new Projectiles(scArrowEntity, arrowCount + 1);
        }

        public Projectiles unloadProjectile() {
            return new Projectiles(scArrowEntity, Math.max(arrowCount - 1, 0));
        }

        public void applyToNbt(NbtCompound nbt) {
            nbt.putInt(scArrowEntity.getEntityName(), arrowCount);
        }

        public int getArrowCount() {
            return arrowCount;
        }
    }

    public record WeaponState(boolean isReloading, boolean isCharged, boolean isShooting) {
        public static WeaponState fromNbt(NbtCompound nbt) {
            return new WeaponState(
                    nbt.getBoolean("sc_reload"),
                    nbt.getBoolean("sc_charged"),
                    nbt.getBoolean("sc_shoot")
            );
        }

        public void applyToNbt(NbtCompound nbt) {
            nbt.putBoolean("sc_reload", isReloading);
            nbt.putBoolean("sc_charged", isCharged);
            nbt.putBoolean("sc_shoot", isShooting);
        }
    }

    private static Item[] getItemsFromIds(Set<String> itemIds) {
        return itemIds.stream()
                .map(Identifier::new)
                .map(Registries.ITEM::get)
                .toArray(Item[]::new);
    }

    public static AmmoRequirement getAmmoRequirement(Item item) {
        WeaponDefinitionsLoader.DefinitionData definitionData = WeaponDefinitionsLoader.getData(item);
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

    public record AmmoRequirement(
            int amountFirstItem, Item firstItem, Item firstItem2nOption,
            int amountSecondItem, Item secondItem, Item secondItem2nOption,
            int amountThirdItem, Item thirdItem, Item thirdItem2nOption
    ) {}
}