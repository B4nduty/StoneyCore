package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.entity.custom.SCArrowEntity;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import banduty.stoneycore.items.item.SCArrow;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.util.definitionsloader.SCRangedWeaponDefinitionsLoader;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public final class SCRangeWeaponUtil {
    private static final Random random = new Random();

    private SCRangeWeaponUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static SCRangedWeaponDefinitionsLoader.DefinitionData getDefinitionData(Item item) {
        Identifier itemId = Registries.ITEM.getId(item);
        Identifier definitionId = Identifier.of(itemId.getNamespace(), itemId.getPath());
        return SCRangedWeaponDefinitionsLoader.getData(definitionId);
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
                projectiles.unloadProjectile();
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
        arrowEntity.setDamageAmount(getDefinitionData(stack.getItem()).baseDamage());
        arrowEntity.setDamageType(getDefinitionData(stack.getItem()).damageType());

        arrowEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, pullProgress * getDefinitionData(stack.getItem()).speed(), 1.0F);

        if (player.isCreative()) {
            arrowEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        } else {
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
            player.getInventory().removeStack(getArrowSlot(player), 1);
        }

        world.spawnEntity(arrowEntity);
        float volume;
        if (world instanceof ClientWorld clientWorld) {
            for (PlayerEntity playerEntity : clientWorld.getPlayers()) {
                if (playerEntity != null) {
                    Vec3d playerPos = player.getPos();
                    Vec3d hearPos = playerEntity.getPos();
                    double distance = playerPos.distanceTo(hearPos);
                    volume = (float) Math.max(0, 1 - (distance * 0.01));
                    if (world.isClient() && volume != 0) {
                        SCRangedWeaponDefinitionsLoader.DefinitionData definitionData = SCRangeWeaponUtil.getDefinitionData(stack.getItem());
                        int soundEventsLength = definitionData.soundEvents().length;
                        SoundEvent selectedSound = soundEventsLength > 0 ? definitionData.soundEvents()[random.nextInt(soundEventsLength)] : null;
                        if (selectedSound != null) player.playSound(selectedSound, SoundCategory.PLAYERS, volume, 1.0F);
                    }
                }
            }
        }
    }

    public static void shootBullet(World world, ItemStack stack, PlayerEntity player) {
        SCBulletEntity bulletEntity = new SCBulletEntity(player, world);
        bulletEntity.setDamageAmount(getDefinitionData(stack.getItem()).baseDamage());
        bulletEntity.setDamageType(getDefinitionData(stack.getItem()).damageType());

        bulletEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, getDefinitionData(stack.getItem()).speed(), 1.0F);

        world.spawnEntity(bulletEntity);
        float volume;
        if (world instanceof ClientWorld clientWorld) {
            for (PlayerEntity playerEntity : clientWorld.getPlayers()) {
                if (playerEntity != null) {
                    Vec3d playerPos = player.getPos();
                    Vec3d hearPos = playerEntity.getPos();
                    double distance = playerPos.distanceTo(hearPos);
                    volume = (float) Math.max(0, 1 - (distance * 0.01));
                    if (world.isClient() && volume != 0) {
                        SCRangedWeaponDefinitionsLoader.DefinitionData definitionData = SCRangeWeaponUtil.getDefinitionData(stack.getItem());
                        int soundEventsLength = definitionData.soundEvents().length;
                        SoundEvent selectedSound = soundEventsLength > 0 ? definitionData.soundEvents()[random.nextInt(soundEventsLength)] : null;
                        if (selectedSound != null) player.playSound(selectedSound, SoundCategory.PLAYERS, volume, 1.0F);
                    }
                }
            }
        }

        if (!player.isCreative()) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
        }

        if (world instanceof ServerWorld serverWorld) {
            spawnSmokeTrail(serverWorld, player, player.getActiveHand());
            spawnFlashTrail(serverWorld, player, player.getActiveHand());
        }
    }

    private static void spawnSmokeTrail(ServerWorld world, PlayerEntity player, Hand hand) {
        Vec3d handPos = getHandPosition(player, hand);
        Vec3d lookDir = player.getRotationVec(1.0F);

        List<Vec3d> trailPositions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            trailPositions.add(handPos.add(lookDir.multiply(i)));
        }

        for (Vec3d pos : trailPositions) {
            world.spawnParticles(ModParticles.MUZZLES_SMOKE_PARTICLE, pos.x, pos.y, pos.z, 100,
                    0.2, 0.1, 0.2, 0.0005f);
        }
    }

    private static void spawnFlashTrail(ServerWorld world, PlayerEntity player, Hand hand) {
        Vec3d handPos = getHandPosition(player, hand);
        Vec3d lookDir = player.getRotationVec(1.0F);

        List<Vec3d> trailPositions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            trailPositions.add(handPos.add(lookDir.multiply(i)));
        }

        for (Vec3d pos : trailPositions) {
            world.spawnParticles(ModParticles.MUZZLES_FLASH_PARTICLE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.1);
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
        return Math.min((float) useTicks / (getDefinitionData(item).rechargeTime() * 20), 1.0F);
    }

    public static WeaponState getWeaponState(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return WeaponState.fromNbt(nbt);
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

        public void unloadProjectile() {
            new Projectiles(scArrowEntity, Math.max(arrowCount - 1, 0));
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

    public static AmmoRequirement getAmmoRequirement(Item item) {
        SCRangedWeaponDefinitionsLoader.DefinitionData definitionData = getDefinitionData(item);
        Map<String, SCRangedWeaponDefinitionsLoader.AmmoRequirementData> ammoRequirementMap = definitionData.ammoRequirement();

        int amountFirstItem = 0;
        Item firstItem = null;
        Item firstItem2nOption = null;

        int amountSecondItem = 0;
        Item secondItem = null;
        Item secondItem2nOption = null;

        int amountThirdItem = 0;
        Item thirdItem = null;
        Item thirdItem2nOption = null;

        if (ammoRequirementMap.containsKey("item1")) {
            SCRangedWeaponDefinitionsLoader.AmmoRequirementData item1Data = ammoRequirementMap.get("item1");
            Set<String> item1Ids = item1Data.itemIds();
            amountFirstItem = item1Data.amount();

            firstItem = Registries.ITEM.get(new Identifier(item1Ids.iterator().next()));

            if (item1Ids.size() > 1) {
                firstItem2nOption = Registries.ITEM.get(new Identifier(item1Ids.stream().skip(1).findFirst().get()));
            }
        }

        if (ammoRequirementMap.containsKey("item2")) {
            SCRangedWeaponDefinitionsLoader.AmmoRequirementData item2Data = ammoRequirementMap.get("item2");
            Set<String> item2Ids = item2Data.itemIds();
            amountSecondItem = item2Data.amount();

            secondItem = Registries.ITEM.get(new Identifier(item2Ids.iterator().next()));

            if (item2Ids.size() > 1) {
                secondItem2nOption = Registries.ITEM.get(new Identifier(item2Ids.stream().skip(1).findFirst().get()));
            }
        }

        if (ammoRequirementMap.containsKey("item3")) {
            SCRangedWeaponDefinitionsLoader.AmmoRequirementData item3Data = ammoRequirementMap.get("item3");
            Set<String> item3Ids = item3Data.itemIds();
            amountThirdItem = item3Data.amount();

            thirdItem = Registries.ITEM.get(new Identifier(item3Ids.iterator().next()));

            if (item3Ids.size() > 1) {
                thirdItem2nOption = Registries.ITEM.get(new Identifier(item3Ids.stream().skip(1).findFirst().get()));
            }
        }

        return amountFirstItem >= 1
                ? new AmmoRequirement(
                amountFirstItem, firstItem, firstItem2nOption,
                amountSecondItem, secondItem, secondItem2nOption,
                amountThirdItem, thirdItem, thirdItem2nOption
        )
                : null;
    }

    public record AmmoRequirement(
            int amountFirstItem, Item firstItem, Item firstItem2nOption,
            int amountSecondItem, Item secondItem, Item secondItem2nOption,
            int amountThirdItem, Item thirdItem, Item thirdItem2nOption
    ) {}
}
