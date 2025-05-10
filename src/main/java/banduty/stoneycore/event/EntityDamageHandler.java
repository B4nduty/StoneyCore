package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.event.custom.LivingEntityDamageEvents;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class EntityDamageHandler implements LivingEntityDamageEvents {
    private static final float STRENGTH_MULTIPLIER = 3.0F;
    private static final float WEAKNESS_MULTIPLIER = 4.0F;
    private static final int PARRY_WINDOW_TICKS = 10;
    private static final float PARRY_KNOCKBACK_STRENGTH = 0.5F;

    @Override
    public float onDamage(LivingEntity target, DamageSource source, float amount) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) {
            return amount;
        }

        if (handleParry(target, attacker, source)) {
            return 0.0F;
        }

        ItemStack stack = attacker.getMainHandStack();
        if (target instanceof ServerPlayerEntity playerEntity && StaminaData.isStaminaBlocked((IEntityDataSaver) playerEntity) && StoneyCore.getConfig().getRealisticCombat()) {
            ItemStack handStack = playerEntity.getMainHandStack();
            if (!handStack.isEmpty()) {
                playerEntity.dropItem(handStack, false, true);
                playerEntity.setStackInHand(playerEntity.getActiveHand(), ItemStack.EMPTY);
            }
        }

        if (stack.isIn(SCTags.WEAPONS_IGNORES_ARMOR.getTag())) {
            SCDamageCalculator.applyDamage(target, attacker, stack, amount);
            return 0;
        }

        if (SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem())) {
            amount = calculateWeaponDamage(attacker, target, stack.getItem(), stack, amount);
        }

        amount = applyStatusEffectModifiers(attacker, amount);
        return Math.max(amount, 0.0F);
    }

    private boolean handleParry(LivingEntity target, LivingEntity attacker, DamageSource source) {
        if (!StoneyCore.getConfig().getParry() || !(target instanceof PlayerEntity player)) {
            return false;
        }

        if (!player.isBlocking()) {
            return false;
        }

        NbtCompound persistentData = ((IEntityDataSaver) player).stoneycore$getPersistentData();
        if (!persistentData.contains("BlockStartTick")) {
            return false;
        }

        int blockStartTick = persistentData.getInt("BlockStartTick");
        int currentTick = (int) player.getWorld().getTime();

        if (currentTick - blockStartTick > PARRY_WINDOW_TICKS || source.isIn(DamageTypeTags.IS_EXPLOSION) || source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }

        performParryEffects(player, attacker);
        StaminaData.removeStamina((IEntityDataSaver) player, StoneyCore.getConfig().onParryStamina());
        return true;
    }

    private void performParryEffects(PlayerEntity player, LivingEntity attacker) {
        Vec3d playerPos = player.getPos();
        Vec3d attackerPos = attacker.getPos();
        Vec3d knockbackDirection = playerPos.subtract(attackerPos).normalize();

        attacker.takeKnockback(PARRY_KNOCKBACK_STRENGTH, knockbackDirection.x, knockbackDirection.z);

        player.getWorld().playSound(
                null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1.0F, 1.5F
        );
    }

    private float calculateWeaponDamage(LivingEntity attacker, LivingEntity target,
                                        Item item, ItemStack stack, float originalDamage) {
        int comboCount = attacker instanceof PlayerEntity player ? ((PlayerAttackProperties) player).getComboCount() : 0;

        SCDamageCalculator.DamageType damageType = SCWeaponUtil.calculateDamageType(stack, item, comboCount);

        double maxDistance = SCWeaponUtil.getMaxDistance(stack.getItem());
        double actualDistance = attacker.getPos().distanceTo(target.getPos());

        if (actualDistance > maxDistance + 1) {
            return originalDamage;
        }

        float baseDamage = SCWeaponUtil.calculateDamage(stack.getItem(), actualDistance, damageType.name());

        float calculatedDamage = SCDamageCalculator.getSCDamage(target, baseDamage, damageType);

        if (stack.isIn(SCTags.WEAPONS_DAMAGE_BEHIND.getTag())) {
            calculatedDamage = SCWeaponUtil.adjustDamageForBackstab(target, attacker.getPos(), calculatedDamage);
        }

        return calculatedDamage != 0 ? calculatedDamage : originalDamage;
    }

    private float applyStatusEffectModifiers(LivingEntity attacker, float damage) {
        StatusEffectInstance strength = attacker.getStatusEffect(StatusEffects.STRENGTH);
        StatusEffectInstance weakness = attacker.getStatusEffect(StatusEffects.WEAKNESS);

        if (strength != null) {
            damage += STRENGTH_MULTIPLIER * (strength.getAmplifier() + 1);
        }

        if (weakness != null) {
            damage -= WEAKNESS_MULTIPLIER * (weakness.getAmplifier() + 1);
        }

        return damage;
    }
}