package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.config.IConfig;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.streq.StrEq;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class StaminaUtil {
    public static void startStaminaTrack(LivingEntity entity) {
        double maxStamina = entity.getAttributeValue(Services.ATTRIBUTES.getMaxStamina());
        double currentStamina = StaminaData.getStamina(entity);
        boolean isCreativeOrSpectator = entity instanceof Player player &&
                (player.isCreative() || player.isSpectator());

        if (isCreativeOrSpectator || maxStamina <= 0) {
            if (currentStamina < maxStamina) {
                StaminaData.setStamina(entity, maxStamina);
            }
            removeStaminaEffects(entity);
            StaminaData.setStaminaBlocked((IEntityDataSaver) entity, false);
            StaminaData.setStaminaUseTime((IEntityDataSaver) entity, 0);
            return;
        }

        if (currentStamina > maxStamina) {
            StaminaData.setStamina(entity, maxStamina);
        }

        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
        boolean wasUsingStamina = isUsingStamina(entity);

        if (wasUsingStamina) {
            StaminaData.setStaminaUseTime(dataSaver, StoneyCore.getConfig().combatOptions().getStaminaRecoverTime());
        }

        boolean canRecoverStamina = StaminaData.getStaminaUseTime(dataSaver) <= 0;

        if (StaminaData.getStaminaUseTime(dataSaver) > 0) StaminaData.setStaminaUseTime(dataSaver, StaminaData.getStaminaUseTime(dataSaver) - 1);

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

        int recoveryRate = Math.max(1, (int) StrEq.evaluate(config.staminaRecoveryFormula(), vars));

        if (entity.tickCount % recoveryRate != 0) return;

        double maxStamina = entity.getAttributeValue(Services.ATTRIBUTES.getMaxStamina());
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
        IConfig.CombatOptions config = StoneyCore.getConfig().combatOptions();

        if (isSCWeapon(entity.getMainHandItem()) && entity.isBlocking()) { // Blocking
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
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, -1, fatigueLevel, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, -1, slownessLevel, false, false, false));
    }

    private static void removeStaminaEffects(LivingEntity entity) {
        entity.removeEffect(MobEffects.DIG_SLOWDOWN);
        entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    private static boolean isSCWeapon(ItemStack stack) {
        return WeaponDefinitionsStorage.isMelee(stack);
    }

    private static boolean isWearingSCArmor(LivingEntity entity) {
        for (ItemStack armorStack : entity.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(armorStack.getItem())) {
                return true;
            }
        }
        return false;
    }
}