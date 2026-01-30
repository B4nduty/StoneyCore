package banduty.stoneycore.config;

import banduty.stoneycore.config.StoneyCoreConfig;

public class FabricConfigImpl extends ConfigImpl {
    private final StoneyCoreConfig config;

    public FabricConfigImpl() {
        this.config = StoneyCoreConfig.createAndLoad();
    }

    @Override
    public IConfig.CombatOptions combatOptions() {
        return new IConfig.CombatOptions() {
            @Override
            public boolean getRealisticCombat() {
                return config.combatOptions.getRealisticCombat();
            }

            @Override
            public int getToggleVisorTime() {
                return config.combatOptions.getToggleVisorTime();
            }

            @Override
            public boolean getParry() {
                return config.combatOptions.getParry();
            }

            @Override
            public float maxBaseStamina() {
                return config.combatOptions.maxBaseStamina();
            }

            @Override
            public String staminaRecoveryFormula() {
                return config.combatOptions.staminaRecoveryFormula();
            }

            @Override
            public double blockingStaminaConstant() {
                return config.combatOptions.blockingStaminaConstant();
            }

            @Override
            public double onBlockStaminaConstant() {
                return config.combatOptions.onBlockStaminaConstant();
            }

            @Override
            public double onParryStaminaConstant() {
                return config.combatOptions.onParryStaminaConstant();
            }

            @Override
            public double sprintingStaminaConstant() {
                return config.combatOptions.sprintingStaminaConstant();
            }

            @Override
            public double jumpingStaminaConstant() {
                return config.combatOptions.jumpingStaminaConstant();
            }

            @Override
            public double swimmingStaminaConstant() {
                return config.combatOptions.swimmingStaminaConstant();
            }

            @Override
            public double attackStaminaConstant() {
                return config.combatOptions.attackStaminaConstant();
            }

            @Override
            public int getStaminaRecoverTime() {
                return config.combatOptions.getStaminaRecoverTime();
            }
        };
    }

    @Override
    public IConfig.VisualOptions visualOptions() {
        return new IConfig.VisualOptions() {
            @Override
            public boolean getDamageIndicator() {
                return config.visualOptions.getDamageIndicator();
            }

            @Override
            public boolean getVisoredHelmet() {
                return config.visualOptions.getVisoredHelmet();
            }

            @Override
            public float getVisoredHelmetAlphaCreative() {
                return config.visualOptions.getVisoredHelmetAlphaCreative();
            }

            @Override
            public float getVisoredHelmetAlphaSurvival() {
                return config.visualOptions.getVisoredHelmetAlphaSurvival();
            }

            @Override
            public boolean getLowStaminaIndicator() {
                return config.visualOptions.getLowStaminaIndicator();
            }

            @Override
            public boolean getNoiseEffect() {
                return config.visualOptions.getNoiseEffect();
            }

            @Override
            public int getMuzzlesSmokeParticlesTime() {
                return config.visualOptions.getMuzzlesSmokeParticlesTime();
            }

            @Override
            public int getStaminaBarYOffset() {
                return config.visualOptions.getStaminaBarYOffset();
            }

            @Override
            public int hexColorTooFarClose() {
                return config.visualOptions.hexColorTooFarClose();
            }

            @Override
            public int hexColorEffective() {
                return config.visualOptions.hexColorEffective();
            }

            @Override
            public int hexColorCritical() {
                return config.visualOptions.hexColorCritical();
            }

            @Override
            public int hexColorMaximum() {
                return config.visualOptions.hexColorMaximum();
            }

            @Override
            public boolean armWave() {
                return config.visualOptions.armWave();
            }

            @Override
            public int claimOutlineColor() {
                return config.visualOptions.claimOutlineColor();
            }
        };
    }

    @Override
    public IConfig.TechnicalOptions technicalOptions() {
        return new IConfig.TechnicalOptions() {
            @Override
            public int maxWorkPerTick() {
                return config.technicalOptions.maxWorkPerTick();
            }

            @Override
            public int maxLandExpandRadius() {
                return config.technicalOptions.maxLandExpandRadius();
            }

            @Override
            public boolean breakOrRemoveSiegeDestroy() {
                return config.technicalOptions.breakOrRemoveSiegeDestroy();
            }
        };
    }

    @Override
    public IConfig.LandOptions landOptions() {
        return new IConfig.LandOptions() {
            @Override
            public boolean claimLand() {
                return config.landOptions.claimLand();
            }

            @Override
            public boolean removeClaimedSiege() {
                return config.landOptions.removeClaimedSiege();
            }

            @Override
            public boolean hungerSiege() {
                return config.landOptions.hungerSiege();
            }
        };
    }
}