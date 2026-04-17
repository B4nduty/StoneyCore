package banduty.stoneycore.config;

public interface IConfig {
    interface CombatOptions {
        boolean getRealisticCombat();

        int getToggleVisorTime();

        boolean getParry();

        float maxBaseStamina();

        String staminaRecoveryFormula();

        double blockingStaminaConstant();

        double onBlockStaminaConstant();

        double onParryStaminaConstant();

        double sprintingStaminaConstant();

        double jumpingStaminaConstant();

        double swimmingStaminaConstant();

        double attackStaminaConstant();

        int getStaminaRecoverTime();
    }

    interface VisualOptions {
        boolean getDamageIndicator();

        boolean getVisoredHelmet();

        float getVisoredHelmetAlphaCreative();

        float getVisoredHelmetAlphaSurvival();

        boolean overlayThirdPerson();

        boolean getLowStaminaIndicator();

        boolean getNoiseEffect();

        int getMuzzlesSmokeParticlesTime();

        int getStaminaBarYOffset();

        int hexColorTooFarClose();

        int claimOutlineColor();
    }

    interface TechnicalOptions {
        int maxWorkPerTick();

        int maxLandExpandRadius();

        boolean breakOrRemoveSiegeDestroy();
    }

    interface LandOptions {
        boolean claimLand();

        boolean removeClaimedSiege();

        boolean hungerSiege();

        boolean landVisitors();
    }

    CombatOptions combatOptions();

    VisualOptions visualOptions();

    TechnicalOptions technicalOptions();

    LandOptions landOptions();
}