package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.config.StoneyCoreConfig;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.playerdata.SCAttributes;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import banduty.streq.StrEq;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class StaminaUtil {
    public static void startStaminaTrack(LivingEntity livingEntity) {
        double stamina = StaminaData.getStamina(livingEntity);

        if ((livingEntity instanceof PlayerEntity playerEntity && (playerEntity.isCreative() || livingEntity.isSpectator()) || livingEntity.getAttributeValue(SCAttributes.MAX_STAMINA) <= 0)) {
            if (stamina < livingEntity.getAttributeValue(SCAttributes.MAX_STAMINA)) StaminaData.setStamina(livingEntity, livingEntity.getAttributeValue(SCAttributes.MAX_STAMINA));
            removeStaminaEffects(livingEntity);
            StaminaData.setStaminaBlocked((IEntityDataSaver) livingEntity, false);
        }

        if (stamina > livingEntity.getAttributeValue(SCAttributes.MAX_STAMINA))
            StaminaData.setStamina(livingEntity, livingEntity.getAttributeValue(SCAttributes.MAX_STAMINA));

        if (!(livingEntity instanceof PlayerEntity playerEntity && (playerEntity.isCreative() || livingEntity.isSpectator()))) {
            if (!StoneyCore.getConfig().getRealisticCombat() || !isUsingStamina(livingEntity) || livingEntity.isOnGround() || livingEntity.isClimbing()) handleStaminaRecovery(livingEntity, stamina);
            handleStaminaEffects(livingEntity, stamina);
        }
    }

    private static void handleStaminaRecovery(LivingEntity livingEntity, double stamina) {
        double foodLevel = livingEntity instanceof PlayerEntity playerEntity ? playerEntity.getHungerManager().getFoodLevel() : 20;
        double health = livingEntity.getHealth();
        String formula = StoneyCore.getConfig().staminaRecoveryFormula();
        Map<String, Double> variables = new HashMap<>();
        variables.put("foodLevel", foodLevel);
        variables.put("health", health);
        int ticksPerRecovery = Math.max(1, (int) StrEq.evaluate(formula, variables));

        StaminaData.addStamina(livingEntity, 0);
        if (livingEntity.age % ticksPerRecovery == 0 && stamina < livingEntity.getAttributeValue(SCAttributes.MAX_STAMINA) && !(foodLevel == 0 && StoneyCore.getConfig().getRealisticCombat())) {
            StaminaData.addStamina(livingEntity, 0.1d);
        }
    }

    private static void handleStaminaEffects(LivingEntity livingEntity, double stamina) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) livingEntity;
        long firstLevel = Math.absExact((int) (livingEntity.getAttributeBaseValue(SCAttributes.MAX_STAMINA) * 0.3d));
        long secondLevel = Math.absExact((int) (livingEntity.getAttributeBaseValue(SCAttributes.MAX_STAMINA) * 0.15d));

        if (stamina < firstLevel && stamina > secondLevel) {
            applyStaminaEffects(livingEntity, 0, 0);
        }

        if (stamina == 0) {
            StaminaData.setStaminaBlocked(dataSaver, true);
            applyStaminaEffects(livingEntity, 3, 3);
        }

        if (StaminaData.isStaminaBlocked((IEntityDataSaver) livingEntity) && stamina >= secondLevel) {
            StaminaData.setStaminaBlocked(dataSaver, false);
            removeStaminaEffects(livingEntity);
        }

        if (stamina >= firstLevel) {
            removeStaminaEffects(livingEntity);
        }
    }

    private static boolean isUsingStamina(LivingEntity livingEntity) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) livingEntity;
        boolean staminaBlocked = StaminaData.isStaminaBlocked(dataSaver);
        boolean usingStamina = false;

        boolean wearingSCArmor = isWearingSCArmor(livingEntity);
        boolean hasSCWeapon = isSCWeapon(livingEntity.getMainHandStack());
        StoneyCoreConfig config = StoneyCore.getConfig();

        if (!staminaBlocked) {
            if (hasSCWeapon && livingEntity.isBlocking()) {
                StaminaData.removeStamina(livingEntity, config.blockingStaminaPerSecond() / 20d);
                usingStamina = true;
            }

            if (wearingSCArmor) {
                if (livingEntity.isSprinting()) {
                    StaminaData.removeStamina(livingEntity, config.sprintingStaminaPerSecond() / 20d);
                    usingStamina = true;
                }

                if (livingEntity.isSwimming()) {
                    StaminaData.removeStamina(livingEntity, config.swimmingStaminaPerSecond() / 20d);
                    usingStamina = true;
                }
            }
        }

        return usingStamina;
    }

    private static void applyStaminaEffects(LivingEntity livingEntity, int miningFatigueLevel, int slownessLevel) {
        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, -1,
                miningFatigueLevel, false, false, false));
        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, -1,
                slownessLevel, false, false, false));
    }

    private static void removeStaminaEffects(LivingEntity livingEntity) {
        livingEntity.removeStatusEffect(StatusEffects.SLOWNESS);
        livingEntity.removeStatusEffect(StatusEffects.MINING_FATIGUE);
    }

    private static boolean isSCWeapon(ItemStack stack) {
        return SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem());
    }

    private static boolean isWearingSCArmor(LivingEntity livingEntity) {
        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (SCArmorDefinitionsLoader.containsItem(armorStack.getItem())) {
                return true;
            }
        }
        return false;
    }
}
