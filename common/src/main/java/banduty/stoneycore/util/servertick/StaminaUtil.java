package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.config.IConfig;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class StaminaUtil {
    public static void startStaminaTrack(LivingEntity entity) {
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

        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
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

        Difficulty difficulty = entity.level().getDifficulty();

        double difficultyMultiplier = switch (difficulty) {
            case PEACEFUL -> 0.5; // 2x faster
            case EASY -> 1.0;
            case NORMAL -> 1.5;
            case HARD -> 2.0; // 2x slower
        };

        int recoveryRate = Math.max(1, (int) (StoneyCore.getStrEq().evaluate(config.staminaRecoveryFormula(), vars) * difficultyMultiplier)
        );

        if (entity.tickCount % recoveryRate != 0) return;

        double maxStamina = entity.getAttributeValue(SCAttributes.MAX_STAMINA);
        if (currentStamina < maxStamina && (foodLevel > 0 || !config.getRealisticCombat())) {
            StaminaData.addStamina(entity, 0.1d);
        }
    }

    private static void handleStaminaEffects(LivingEntity entity, double currentStamina, double maxStamina) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;

        double level1 = maxStamina * 0.30;
        double level2 = maxStamina * 0.20;
        double level3 = maxStamina * 0.10;

        if (currentStamina <= 0) {
            StaminaData.setStaminaBlocked(dataSaver, true);
            applyStaminaEffects(entity, 3, 3);
        } else if (currentStamina <= level3) {
            applyStaminaEffects(entity, 2, 2);
        } else if (currentStamina <= level2) {
            applyStaminaEffects(entity, 1, 1);
        } else if (currentStamina <= level1) {
            applyStaminaEffects(entity, 0, 0);
        }

        if (StaminaData.isStaminaBlocked(dataSaver) && currentStamina >= level3) {
            StaminaData.setStaminaBlocked(dataSaver, false);
        }
    }

    private static boolean isUsingStamina(LivingEntity entity) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) entity;
        if (StaminaData.isStaminaBlocked(dataSaver)) return false;

        boolean usingStamina = false;
        IConfig.CombatOptions config = StoneyCore.getConfig().combatOptions();

        if (isSCWeapon(entity.getMainHandItem()) && entity.getMainHandItem().is(SCTags.WEAPONS_SHIELD.getTag()) && entity.isUsingItem() && entity.getUseItem() == entity.getMainHandItem()) { // Blocking
            StaminaData.removeStamina(entity, config.blockingStaminaConstant() * WeightUtil.getWeight(entity) / 20.0);
            usingStamina = true;
        }

        if (!isWearingSCArmor(entity)) return usingStamina;

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

    private static void applyStaminaEffects(LivingEntity entity, int fatigueLevel, int slownessLevel) {
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, fatigueLevel, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, slownessLevel, false, false, false));
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