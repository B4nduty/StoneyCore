package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.util.DeflectChanceHelper;
import banduty.stoneycore.util.EntityDamageUtil;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.SCBetterCombat;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IEntityDataSaver {
    @Shadow
    protected abstract void checkFallDamage(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);

    @Unique
    private CompoundTag persistentData;

    LivingEntityMixin(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    @Override
    public CompoundTag stoneycore$getPersistentData() {
        if (persistentData == null) {
            persistentData = new CompoundTag();
        }
        return persistentData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    protected void stoneycore$addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        if (persistentData != null) {
            compoundTag.put("stoneycore.data", persistentData);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    protected void stoneycore$readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo info) {
        if (compoundTag.contains("stoneycore.data", 10)) {
            persistentData = compoundTag.getCompound("stoneycore.data");
        }
    }

    @Inject(
            method = "createLivingAttributes",
            require = 1, allow = 1, at = @At("RETURN"))
    private static void stoneycore$addAttributes(final CallbackInfoReturnable<AttributeSupplier.Builder> info) {
        info.getReturnValue().add(Services.ATTRIBUTES.getHungerDrainMultiplier()).add(Services.ATTRIBUTES.getStamina()).add(Services.ATTRIBUTES.getMaxStamina());
    }

    @Unique
    private boolean blockShield = true;

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onJump(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (stoneyCore$isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void stoneycore$setSprinting(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (stoneyCore$isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "startUsingItem", at = @At("HEAD"), cancellable = true)
    private void stoneycore$startUsingItem(InteractionHand interactionHand, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (stoneyCore$isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "canDisableShield", at = @At("HEAD"), cancellable = true)
    public void stoneycore$canDisableShield(CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainStack = ((LivingEntity) (Object) this).getMainHandItem();
        boolean isWeaponOrInTag = mainStack.getItem() instanceof AxeItem
                || mainStack.is(SCTags.WEAPONS_DISABLE_SHIELD.getTag());

        cir.setReturnValue(isWeaponOrInTag);
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void stoneycore$injectDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.level().isClientSide()) return;
        if (source.getEntity() instanceof LivingEntity attacker) {
            if (attacker.getMainHandItem().is(SCTags.WEAPONS_BYPASS_BLOCK.getTag())) {
                this.blockShield = false;
            }
        }

        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity.getUseItem().is(SCTags.WEAPONS_SHIELD.getTag()) && source.getDirectEntity() instanceof AbstractArrow) {
            this.blockShield = false;
        }
    }

    @Unique
    private DamageSource stoneycore$currentDamageSource;
    @Unique
    private static final int PARRY_WINDOW_TICKS = 10;
    @Unique
    private static final float PARRY_KNOCKBACK_STRENGTH = 0.5F;

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void stoneycore$captureDamageSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.stoneycore$currentDamageSource = source;
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (!(livingEntity.level() instanceof ServerLevel serverLevel)) return;
        if (source.getEntity() == null) return;

        if (stoneyCore$handleParry(livingEntity, source)) {
            cir.cancel();
        }

        ItemStack weaponStack = ItemStack.EMPTY;
        if (Services.PLATFORM.isModLoaded("bettercombat")) SCBetterCombat.getWeaponStack(source.getEntity(), ItemStack.EMPTY);

        if (DeflectChanceHelper.shouldDeflect(livingEntity, weaponStack)) {
            cir.cancel();
        }

        LandState stateManager = LandState.get(serverLevel);
        Optional<Land> maybeLand = stateManager.getLandAt(source.getEntity().getOnPos());
        if (maybeLand.isPresent() && source.getEntity() instanceof Player player &&
                SiegeManager.isPlayerInLandUnderSiege(serverLevel, player) &&
                !SiegeManager.getPlayerSiege(serverLevel, source.getEntity().getUUID())
                        .map(siege -> !siege.disabledPlayers.contains(source.getEntity().getUUID()))
                        .orElse(false)) {
            cir.cancel();
        }

        if (livingEntity instanceof ServerPlayer serverPlayer && StaminaData.isStaminaBlocked((IEntityDataSaver) serverPlayer) &&
                StoneyCore.getConfig().combatOptions().getRealisticCombat()) {
            ItemStack handStack = serverPlayer.getMainHandItem();
            if (!handStack.isEmpty()) {
                serverPlayer.drop(handStack, false, true);
                serverPlayer.setItemInHand(serverPlayer.getUsedItemHand(), ItemStack.EMPTY);
            }
        }
    }

    @Unique
    private static boolean stoneyCore$handleParry(LivingEntity target, DamageSource source) {
        if (!StoneyCore.getConfig().combatOptions().getParry() || !(target instanceof Player player)) {
            return false;
        }

        if (!player.isBlocking()) {
            return false;
        }

        if (source.getDirectEntity() instanceof AbstractArrow) return false;

        long blockStartTick = NBTDataHelper.get((IEntityDataSaver) player, PDKeys.BLOCK_START_TICK, 0L);
        long currentTick = player.level().getGameTime();

        if (currentTick - blockStartTick > PARRY_WINDOW_TICKS) {
            return false;
        }

        if (source.getDirectEntity() == null) return false;

        stoneyCore$performParryEffects(player, source.getDirectEntity());
        StaminaData.removeStamina(player, StoneyCore.getConfig().combatOptions().onParryStaminaConstant() * WeightUtil.getCachedWeight(player));
        return true;
    }

    @Unique
    private static void stoneyCore$performParryEffects(Player player, Entity source) {
        if (source instanceof LivingEntity livingEntity) {
            Vec3 playerPos = player.position();
            Vec3 attackerPos = source.position();
            Vec3 knockbackDirection = playerPos.subtract(attackerPos).normalize();

            livingEntity.knockback(PARRY_KNOCKBACK_STRENGTH, knockbackDirection.x, knockbackDirection.z);
        }

        player.level().playSound(
                null, source.getX(), source.getY(), source.getZ(),
                SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 1.0F, 1.5F
        );
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void stoneycore$knockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        LivingEntityAccessor accessor = (LivingEntityAccessor) livingEntity;

        if (stoneycore$currentDamageSource == null) return;

        Entity attacker = stoneycore$currentDamageSource.getEntity();
        ItemStack weaponStack = ItemStack.EMPTY;
        if (Services.PLATFORM.isModLoaded("bettercombat")) SCBetterCombat.getWeaponStack(attacker, ItemStack.EMPTY);

        if (!(attacker instanceof LivingEntity && !weaponStack.isEmpty() && WeaponDefinitionsStorage.isMelee(weaponStack))) {
            return;
        }

        float damageAmount = accessor.getLastHurt();

        strength *= (damageAmount / 15.0);

        strength = Math.max(strength, 0.1);

        if (EntityDamageUtil.damageType == SCDamageCalculator.DamageType.BLUDGEONING) {
            strength += 0.3f;
        }

        strength += WeaponDefinitionsStorage.getData(weaponStack).melee().bonusKnockback();

        strength *= (double)1.0F - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        if (!(strength <= (double)0.0F)) {
            livingEntity.hasImpulse = true;
            Vec3 vec3 = livingEntity.getDeltaMovement();
            Vec3 vec32 = (new Vec3(x, 0.0F, z)).normalize().scale(strength);
            livingEntity.setDeltaMovement(vec3.x / (double)2.0F - vec32.x,
                    livingEntity.onGround() ? vec3.y / (double)2.0F + strength : vec3.y,
                    vec3.z / (double)2.0F - vec32.z);
        }

        ci.cancel();
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void stoneycore$getDamageAfterArmorAbsorb(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        amount = CombatRules.getDamageAfterAbsorb(amount, (float)livingEntity.getArmorValue(), (float)livingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));

        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (armorStack.isEmpty()) continue;

            EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(armorStack);
            boolean slotProtected = false;

            for (ItemStack itemStack : Services.PLATFORM.getEquippedAccessories(livingEntity)) {
                if (!itemStack.isEmpty() && AccessoriesDefinitionsStorage.containsItem(itemStack)) {
                    String slotFromJson = AccessoriesDefinitionsStorage.getData(itemStack.getItem()).armorSlot();

                    if (!slotFromJson.isBlank() && slotFromJson.equalsIgnoreCase(slot.getName())) {
                        slotProtected = true;
                        itemStack.hurtAndBreak((int) amount, livingEntity, (entity) ->
                                entity.broadcastBreakEvent(EquipmentSlot.MAINHAND)
                        );
                    }
                }
            }

            // Damage the armor only if no accessory protects this slot
            if (!slotProtected) {
                armorStack.hurtAndBreak((int) amount, livingEntity, (entity) ->
                        entity.broadcastBreakEvent(slot)
                );
            }
        }

        cir.setReturnValue(amount);
        cir.cancel();
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void stoneycore$cancelZeroDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount <= 0) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void stoneycore$sendDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (StoneyCore.getConfig().visualOptions().getDamageIndicator() && source.getEntity() instanceof Player player) {
            ItemStack mainHandStack = player.getMainHandItem();
            if (WeaponDefinitionsStorage.isMelee(mainHandStack.getItem()) && !player.hasEffect(MobEffects.WEAKNESS)) {
                if (amount <= 0) amount = 0;
                player.displayClientMessage(Component.literal("Damage: " + (int) amount), true);
            }
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void stoneycore$jumpFromGround(CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity livingEntity) {
            IEntityDataSaver dataSaver = (IEntityDataSaver) livingEntity;
            boolean staminaBlocked = StaminaData.isStaminaBlocked(dataSaver);
            boolean wearingSCArmor = stoneyCore$isWearingSCArmor(livingEntity);
            if (!staminaBlocked && wearingSCArmor) {
                StaminaData.removeStamina(livingEntity, StoneyCore.getConfig().combatOptions().jumpingStaminaConstant() * WeightUtil.getCachedWeight(livingEntity));
            }
        }
    }

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    private void stoneycore$blockedByShield(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (this.level().isClientSide()) return;
        if (!blockShield) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Unique
    private boolean stoneyCore$isStaminaBlocked(LivingEntity livingEntity) {
        return StaminaData.isStaminaBlocked((IEntityDataSaver) livingEntity);
    }

    @Unique
    private boolean stoneyCore$isWearingSCArmor(LivingEntity livingEntity) {
        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(armorStack.getItem())) {
                return true;
            }
        }
        return false;
    }
}