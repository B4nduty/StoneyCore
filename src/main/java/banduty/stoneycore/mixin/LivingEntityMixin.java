package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.util.DeflectChanceHelper;
import banduty.stoneycore.util.EntityDamageUtil;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import banduty.stoneycore.util.data.playerdata.SCAttributes;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.projectile.PersistentProjectileEntity;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IEntityDataSaver {
    @Shadow
    protected abstract void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);

    @Unique
    private NbtCompound persistentData;

    LivingEntityMixin(final EntityType<?> type, final World world) {
        super(type, world);
    }

    @Override
    public NbtCompound stoneycore$getPersistentData() {
        if (persistentData == null) {
            persistentData = new NbtCompound();
        }
        return persistentData;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    protected void stoneycore$injectWriteMethod(NbtCompound nbt, CallbackInfo ci) {
        if (persistentData != null) {
            nbt.put("stoneycore.data", persistentData);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    protected void stoneycore$injectReadMethod(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("stoneycore.data", 10)) {
            persistentData = nbt.getCompound("stoneycore.data");
        }
    }

    @Inject(
            method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN"))
    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        info.getReturnValue().add(SCAttributes.HUNGER_DRAIN_MULTIPLIER).add(SCAttributes.STAMINA).add(SCAttributes.MAX_STAMINA);
    }

    @Unique
    private boolean blockShield = true;

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onJump(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onSprinting(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setCurrentHand", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onSetCurrentHand(Hand hand, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "disablesShield", at = @At("HEAD"), cancellable = true)
    public void stoneycore$disablesShield(CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainStack = ((LivingEntity) (Object) this).getMainHandStack();
        boolean isWeaponOrInTag = mainStack.getItem() instanceof AxeItem
                || mainStack.isIn(SCTags.WEAPONS_DISABLE_SHIELD.getTag());

        cir.setReturnValue(isWeaponOrInTag);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void stoneycore$injectDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.getWorld().isClient()) return;
        if (source.getAttacker() instanceof LivingEntity attacker) {
            if (attacker.getMainHandStack().isIn(SCTags.WEAPONS_BYPASS_BLOCK.getTag())) {
                this.blockShield = false;
            }
        }

        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity.getActiveItem().isIn(SCTags.WEAPONS_SHIELD.getTag()) && source.getSource() instanceof PersistentProjectileEntity) {
            this.blockShield = false;
        }
    }

    @Unique
    private DamageSource stoneycore$currentDamageSource;
    @Unique
    private static final int PARRY_WINDOW_TICKS = 10;
    @Unique
    private static final float PARRY_KNOCKBACK_STRENGTH = 0.5F;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void stoneycore$captureDamageSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.stoneycore$currentDamageSource = source;
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (!(livingEntity.getWorld() instanceof ServerWorld serverWorld)) return;
        if (source.getAttacker() == null) return;

        if (handleParry(livingEntity, source)) {
            cir.cancel();
        }

        ItemStack weaponStack = getWeaponStack(source.getAttacker());

        if (DeflectChanceHelper.shouldDeflect(livingEntity, weaponStack)) {
            cir.cancel();
        }

        LandState stateManager = LandState.get(serverWorld);
        Optional<Land> maybeLand = stateManager.getLandAt(source.getAttacker().getBlockPos());
        if (maybeLand.isPresent() && source.getAttacker() instanceof PlayerEntity player &&
                SiegeManager.isPlayerInLandUnderSiege(serverWorld, player) &&
                !SiegeManager.getPlayerSiege(serverWorld, source.getAttacker().getUuid())
                        .map(siege -> !siege.disabledPlayers.contains(source.getAttacker().getUuid()))
                        .orElse(false)) {
            cir.cancel();
        }

        if (livingEntity instanceof ServerPlayerEntity playerEntity && StaminaData.isStaminaBlocked((IEntityDataSaver) playerEntity) &&
                StoneyCore.getConfig().combatOptions.getRealisticCombat()) {
            ItemStack handStack = playerEntity.getMainHandStack();
            if (!handStack.isEmpty()) {
                playerEntity.dropItem(handStack, false, true);
                playerEntity.setStackInHand(playerEntity.getActiveHand(), ItemStack.EMPTY);
            }
        }
    }

    @Unique
    private static boolean handleParry(LivingEntity target, DamageSource source) {
        if (!StoneyCore.getConfig().combatOptions.getParry() || !(target instanceof PlayerEntity player)) {
            return false;
        }

        if (!player.isBlocking()) {
            return false;
        }

        if (source.getSource() instanceof PersistentProjectileEntity) return false;

        long blockStartTick = NBTDataHelper.get((IEntityDataSaver) player, PDKeys.BLOCK_START_TICK, 0L);
        long currentTick = player.getWorld().getTime();

        if (currentTick - blockStartTick > PARRY_WINDOW_TICKS) {
            return false;
        }

        if (source.getSource() == null) return false;

        performParryEffects(player, source.getSource());
        StaminaData.removeStamina(player, StoneyCore.getConfig().combatOptions.onParryStaminaConstant() * WeightUtil.getCachedWeight(player));
        return true;
    }

    @Unique
    private static void performParryEffects(PlayerEntity player, Entity source) {
        if (source instanceof LivingEntity livingEntity) {
            Vec3d playerPos = player.getPos();
            Vec3d attackerPos = source.getPos();
            Vec3d knockbackDirection = playerPos.subtract(attackerPos).normalize();

            livingEntity.takeKnockback(PARRY_KNOCKBACK_STRENGTH, knockbackDirection.x, knockbackDirection.z);
        }

        player.getWorld().playSound(
                null, source.getX(), source.getY(), source.getZ(),
                SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1.0F, 1.5F
        );
    }

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void stoneycore$takeKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        LivingEntityAccessor accessor = (LivingEntityAccessor) livingEntity;

        if (stoneycore$currentDamageSource == null) return;

        Entity attacker = stoneycore$currentDamageSource.getAttacker();
        ItemStack weaponStack = getWeaponStack(attacker);

        if (!(attacker instanceof LivingEntity && !weaponStack.isEmpty() && WeaponDefinitionsLoader.isMelee(weaponStack))) {
            return;
        }

        float damageAmount = accessor.getLastDamageTaken();

        strength *= (damageAmount / 15.0);

        strength = Math.max(strength, 0.1);

        if (EntityDamageUtil.damageType == SCDamageCalculator.DamageType.BLUDGEONING) {
            strength += 0.3f;
        }

        strength += WeaponDefinitionsLoader.getData(weaponStack).melee().bonusKnockback();

        strength *= (double)1.0F - livingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        if (!(strength <= (double)0.0F)) {
            livingEntity.velocityDirty = true;
            Vec3d vec3d = livingEntity.getVelocity();
            Vec3d vec3d2 = (new Vec3d(x, 0.0F, z)).normalize().multiply(strength);
            livingEntity.setVelocity(vec3d.x / (double)2.0F - vec3d2.x,
                    livingEntity.isOnGround() ? vec3d.y / (double)2.0F + strength : vec3d.y,
                    vec3d.z / (double)2.0F - vec3d2.z);
        }

        ci.cancel();
    }

    @Unique
    private ItemStack getWeaponStack(Entity attacker) {
        AttackHand hand = null;
        if (attacker instanceof PlayerEntity player) {
            if (player instanceof PlayerAttackProperties props) {
                hand = PlayerAttackHelper.getCurrentAttack(player, props.getComboCount());
            }
        }
        ItemStack itemStack = ItemStack.EMPTY;
        if (hand != null) itemStack = hand.itemStack();
        return itemStack;
    }

    @Inject(method = "applyArmorToDamage", at = @At("HEAD"), cancellable = true)
    private void stoneycore$applyArmorToDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        amount = DamageUtil.getDamageLeft(amount, (float)livingEntity.getArmor(), (float)livingEntity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));

        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (armorStack.isEmpty()) continue;

            EquipmentSlot slot = LivingEntity.getPreferredEquipmentSlot(armorStack);
            boolean slotProtected = false;

            if (AccessoriesCapability.getOptionally(livingEntity).isPresent()) {
                for (SlotEntryReference equipped : AccessoriesCapability.get(livingEntity).getAllEquipped()) {
                    if (!equipped.stack().isEmpty() && AccessoriesDefinitionsLoader.containsItem(equipped.stack())) {
                        String slotFromJson = AccessoriesDefinitionsLoader.getData(equipped.stack().getItem()).armorSlot();

                        if (!slotFromJson.isBlank() && slotFromJson.equalsIgnoreCase(slot.getName())) {
                            slotProtected = true;
                            equipped.stack().damage((int) amount, livingEntity, (entity) ->
                                    entity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                            );
                        }
                    }
                }
            }

            // Damage the armor only if no accessory protects this slot
            if (!slotProtected) {
                armorStack.damage((int) amount, livingEntity, (entity) ->
                        entity.sendEquipmentBreakStatus(slot)
                );
            }
        }

        cir.setReturnValue(amount);
        cir.cancel();
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void stoneycore$cancelZeroDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount <= 0) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void stoneycore$sendDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (StoneyCore.getConfig().visualOptions.getDamageIndicator() && source.getAttacker() instanceof PlayerEntity player) {
            ItemStack mainHandStack = player.getMainHandStack();
            if (WeaponDefinitionsLoader.isMelee(mainHandStack.getItem()) && !player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                if (amount <= 0) amount = 0;
                player.sendMessage(Text.literal("Damage: " + (int) amount), true);
            }
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void stoneycore$jump(CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity livingEntity) {
            IEntityDataSaver dataSaver = (IEntityDataSaver) livingEntity;
            boolean staminaBlocked = StaminaData.isStaminaBlocked(dataSaver);
            boolean wearingSCArmor = isWearingSCArmor(livingEntity);
            if (!staminaBlocked && wearingSCArmor) {
                StaminaData.removeStamina(livingEntity, StoneyCore.getConfig().combatOptions.jumpingStaminaConstant() * WeightUtil.getCachedWeight(livingEntity));
            }
        }
    }

    @Inject(method = "blockedByShield", at = @At("HEAD"), cancellable = true)
    private void stoneycore$blockedByShield(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (this.getWorld().isClient()) return;
        if (!blockShield) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Unique
    private boolean isStaminaBlocked(LivingEntity livingEntity) {
        return StaminaData.isStaminaBlocked((IEntityDataSaver) livingEntity);
    }

    @Unique
    private boolean isWearingSCArmor(LivingEntity livingEntity) {
        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (ArmorDefinitionsLoader.containsItem(armorStack.getItem())) {
                return true;
            }
        }
        return false;
    }
}