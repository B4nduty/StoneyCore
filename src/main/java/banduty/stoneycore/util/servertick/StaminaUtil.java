package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.config.StoneyCoreConfig;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import banduty.streq.StrEq;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class StaminaUtil {
    public static void startStaminaTrack(LivingEntity entity) {
        double maxStamina = entity.getAttributeValue(StoneyCore.MAX_STAMINA.get());
        double currentStamina = StaminaData.getStamina(entity);
        boolean isCreativeOrSpectator = entity instanceof PlayerEntity player &&
                (player.isCreative() || player.isSpectator());

        if (isCreativeOrSpectator || maxStamina <= 0) {
            if (currentStamina < maxStamina) {
                StaminaData.setStamina(entity, maxStamina);
            }
            removeStaminaEffects(entity);
            StaminaData.setStaminaBlocked((IEntityDataSaver) entity, false);
            StaminaData.setLastStaminaUseTime((IEntityDataSaver) entity, 0);
            return;
        }

        if (currentStamina > maxStamina) {
            StaminaData.setStamina(entity, maxStamina);
        }

        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
        boolean wasUsingStamina = isUsingStamina(entity);

        if (wasUsingStamina) {
            StaminaData.setLastStaminaUseTime(dataSaver, entity.age);
        }

        boolean canRecoverStamina = (entity.age - StaminaData.getLastStaminaUseTime(dataSaver)) >= StoneyCore.getConfig().combatOptions.getStaminaRecoverTime();

        boolean skipDrain = !StoneyCore.getConfig().combatOptions.getRealisticCombat() ||
                !wasUsingStamina ||
                entity.isOnGround() || entity.isClimbing();

        if (skipDrain && canRecoverStamina) {
            handleStaminaRecovery(entity, currentStamina);
        }

        handleStaminaEffects(entity, currentStamina, maxStamina);
    }

    private static void handleStaminaRecovery(LivingEntity entity, double currentStamina) {
        StoneyCoreConfig.CombatOptions config = StoneyCore.getConfig().combatOptions;

        double foodLevel = entity instanceof PlayerEntity player ? player.getHungerManager().getFoodLevel() : 20;
        double health = entity.getHealth();

        Map<String, Double> vars = Map.of("foodLevel", foodLevel, "health", health);
        int recoveryRate = Math.max(1, (int) StrEq.evaluate(config.staminaRecoveryFormula(), vars));

        if (entity.age % recoveryRate != 0) return;

        double maxStamina = entity.getAttributeValue(StoneyCore.MAX_STAMINA.get());
        if (currentStamina < maxStamina && (foodLevel > 0 || !config.getRealisticCombat())) {
            StaminaData.addStamina(entity, 0.1d);
        }
    }

    private static void handleStaminaEffects(LivingEntity entity, double currentStamina, double maxStamina) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
        double level1 = maxStamina * 0.3;
        double level2 = maxStamina * 0.15;

        if (currentStamina < level1 && currentStamina > level2) {
            applyStaminaEffects(entity, 0, 0);
        } else if (currentStamina <= 0) {
            StaminaData.setStaminaBlocked(dataSaver, true);
            applyStaminaEffects(entity, 3, 3);
        } else if (StaminaData.isStaminaBlocked(dataSaver) && currentStamina >= level2) {
            StaminaData.setStaminaBlocked(dataSaver, false);
            removeStaminaEffects(entity);
        } else if (currentStamina >= level1) {
            removeStaminaEffects(entity);
        }
    }

    private static boolean isUsingStamina(LivingEntity entity) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
        if (StaminaData.isStaminaBlocked(dataSaver)) return false;

        boolean usingStamina = false;
        StoneyCoreConfig.CombatOptions config = StoneyCore.getConfig().combatOptions;

        if (isSCWeapon(entity.getMainHandStack()) && entity.isBlocking()) { // Blocking
            StaminaData.removeStamina(entity, config.blockingStaminaConstant() * WeightUtil.getCachedWeight(entity) / 20.0);
            usingStamina = true;
        }

        if (!isWearingSCArmor(entity)) return usingStamina;

        if (entity.isSprinting()) { // Running
            StaminaData.removeStamina(entity, config.sprintingStaminaConstant() * WeightUtil.getCachedWeight(entity) / 20.0);
            usingStamina = true;
        }

        if (entity.isSwimming()) { // Swimming
            StaminaData.removeStamina(entity, config.swimmingStaminaConstant() * WeightUtil.getCachedWeight(entity) / 40.0);
            usingStamina = true;
        }

        return usingStamina;
    }

    private static void applyStaminaEffects(LivingEntity entity, int fatigueLevel, int slownessLevel) {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, -1, fatigueLevel, false, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, -1, slownessLevel, false, false, false));
    }

    private static void removeStaminaEffects(LivingEntity entity) {
        entity.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        entity.removeStatusEffect(StatusEffects.SLOWNESS);
    }

    private static boolean isSCWeapon(ItemStack stack) {
        return WeaponDefinitionsLoader.isMelee(stack.getItem());
    }

    private static boolean isWearingSCArmor(LivingEntity entity) {
        for (ItemStack armorStack : entity.getArmorItems()) {
            if (ArmorDefinitionsLoader.containsItem(armorStack.getItem())) {
                return true;
            }
        }
        return false;
    }
}