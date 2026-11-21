package banduty.stoneycore.stamina;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.melee.ShieldUtil;
import banduty.stoneycore.config.IConfig;
import banduty.stoneycore.effect.SCEffects;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.data.IEntityDataSaver;
import banduty.stoneycore.data.SCAttributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class StaminaUtil {
    public static void startStaminaTrack(LivingEntity entity) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
        int combatModeTime = StaminaData.getCombatModeTime(dataSaver);
        if (combatModeTime > 0) StaminaData.setCombatMode(dataSaver, combatModeTime - 1);
        if (StoneyCore.getConfig().combatOptions().disableStamina()) return;
        double maxStamina = entity.getAttributeValue(SCAttributes.MAX_STAMINA);
        double currentStamina = StaminaData.getStamina(entity);
        boolean isCreativeOrSpectator = entity instanceof Player player &&
                (player.isCreative() || player.isSpectator());

        if (isCreativeOrSpectator || maxStamina <= 0) {
            if (currentStamina < maxStamina) {
                StaminaData.setStamina(entity, maxStamina);
            }
            StaminaData.setStaminaBlocked((IEntityDataSaver) entity, false);
            StaminaData.setStaminaUseTime((IEntityDataSaver) entity, 0);
            return;
        }

        if (currentStamina > maxStamina) {
            StaminaData.setStamina(entity, maxStamina);
        }

        boolean wasUsingStamina = isUsingStamina(entity);

        if (wasUsingStamina) {
            StaminaData.setStaminaUseTime(dataSaver, StoneyCore.getConfig().combatOptions().getStaminaRecoverTime());
        }

        boolean canRecoverStamina = StaminaData.getStaminaUseTime(dataSaver) <= 0;

        if (StaminaData.getStaminaUseTime(dataSaver) > 0)
            StaminaData.setStaminaUseTime(dataSaver, StaminaData.getStaminaUseTime(dataSaver) - 1);

        boolean skipDrain = !StoneyCore.getConfig().combatOptions().getRealisticCombat() ||
                !wasUsingStamina ||
                entity.onGround() || entity.onClimbable();

        if (skipDrain && canRecoverStamina) {
            handleStaminaRecovery(entity, currentStamina);
        }

        handleStaminaEffects(entity, currentStamina, maxStamina);
    }

    private static void handleStaminaRecovery(LivingEntity entity, double currentStamina) {
        IConfig.CombatOptions config = StoneyCore.getConfig().combatOptions();

        double foodLevel = entity instanceof Player player ? player.getFoodData().getFoodLevel() : 20;
        double health = entity.getHealth();

        Map<String, Double> vars = new HashMap<>(Map.of(
                "foodLevel", foodLevel,
                "health", health
        ));

        int recoveryRate = Math.max(1, (int) StoneyCore.getStrEq().evaluate(config.staminaRecoveryFormula(), vars));

        if (entity.tickCount % recoveryRate != 0) return;

        double maxStamina = entity.getAttributeValue(SCAttributes.MAX_STAMINA);
        if (currentStamina < maxStamina && (foodLevel > 0 || !config.getRealisticCombat())) {
            StaminaData.addStamina(entity, 0.1d);
        }
    }

    private static void handleStaminaEffects(LivingEntity entity, double currentStamina, double maxStamina) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;

        double level1 = maxStamina * 0.15;

        if (currentStamina <= 0) {
            StaminaData.setStaminaBlocked(dataSaver, true);
            applyStaminaEffects(entity, 1);
        } else if (currentStamina <= level1) {
            applyStaminaEffects(entity, 0);
        }

        if (StaminaData.isStaminaBlocked(dataSaver) && currentStamina >= level1) {
            StaminaData.setStaminaBlocked(dataSaver, false);
        }
    }

    private static boolean isUsingStamina(LivingEntity entity) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
        if (StaminaData.getCombatModeTime(dataSaver) <= 0) return false;
        if (StaminaData.isStaminaBlocked(dataSaver)) return false;

        boolean usingStamina = false;
        IConfig.CombatOptions config = StoneyCore.getConfig().combatOptions();

        if (ShieldUtil.isShield(entity.getMainHandItem()) && entity.isUsingItem() && entity.getUseItem() == entity.getMainHandItem()) { // Blocking
            StaminaData.removeStamina(entity, config.blockingStaminaConstant() * WeightUtil.getWeight(entity) / 20.0);
            usingStamina = true;
        }

        if (entity.isPassenger()) return usingStamina;

        if (entity.isSprinting()) { // Running
            StaminaData.removeStamina(entity, config.sprintingStaminaConstant() * WeightUtil.getWeight(entity) / 20.0);
            usingStamina = true;
        }

        if (entity.isSwimming()) { // Swimming
            StaminaData.removeStamina(entity, config.swimmingStaminaConstant() * WeightUtil.getWeight(entity) / 40.0);
            usingStamina = true;
        }

        return usingStamina;
    }

    private static void applyStaminaEffects(LivingEntity entity, int windedLevel) {
        entity.addEffect(new MobEffectInstance(SCEffects.WINDED, 60, windedLevel, false, false, false));
    }
}