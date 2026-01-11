package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCoreForge;

public class ForgeConfigImpl extends ConfigImpl {
    @Override
    public CombatOptions combatOptions() {
        return new CombatOptions() {
            @Override
            public boolean getRealisticCombat() {
                return StoneyCoreForge.CONFIG.combat.realisticCombat;
            }

            @Override
            public int getToggleVisorTime() {
                return StoneyCoreForge.CONFIG.combat.toggleVisorTime;
            }

            @Override
            public boolean getParry() {
                return StoneyCoreForge.CONFIG.combat.parry;
            }

            @Override
            public float maxBaseStamina() {
                return StoneyCoreForge.CONFIG.combat.maxBaseStamina;
            }

            @Override
            public String staminaRecoveryFormula() {
                return StoneyCoreForge.CONFIG.combat.staminaRecoveryFormula;
            }

            @Override
            public double blockingStaminaConstant() {
                return StoneyCoreForge.CONFIG.combat.blockingStaminaConstant;
            }

            @Override
            public double onBlockStaminaConstant() {
                return StoneyCoreForge.CONFIG.combat.onBlockStaminaConstant;
            }

            @Override
            public double onParryStaminaConstant() {
                return StoneyCoreForge.CONFIG.combat.onParryStaminaConstant;
            }

            @Override
            public double sprintingStaminaConstant() {
                return StoneyCoreForge.CONFIG.combat.sprintingStaminaConstant;
            }

            @Override
            public double jumpingStaminaConstant() {
                return StoneyCoreForge.CONFIG.combat.jumpingStaminaConstant;
            }

            @Override
            public double swimmingStaminaConstant() {
                return StoneyCoreForge.CONFIG.combat.swimmingStaminaConstant;
            }

            @Override
            public double attackStaminaConstant() {
                return StoneyCoreForge.CONFIG.combat.attackStaminaConstant;
            }

            @Override
            public int getStaminaRecoverTime() {
                return StoneyCoreForge.CONFIG.combat.staminaRecoverTime;
            }
        };
    }

    @Override
    public VisualOptions visualOptions() {
        return new VisualOptions() {
            @Override
            public boolean getDamageIndicator() {
                return StoneyCoreForge.CONFIG.visual.damageIndicator;
            }

            @Override
            public boolean getVisoredHelmet() {
                return StoneyCoreForge.CONFIG.visual.visoredHelmet;
            }

            @Override
            public float getVisoredHelmetAlpha() {
                return StoneyCoreForge.CONFIG.visual.visoredHelmetAlpha;
            }

            @Override
            public boolean getLowStaminaIndicator() {
                return StoneyCoreForge.CONFIG.visual.lowStaminaIndicator;
            }

            @Override
            public boolean getNoiseEffect() {
                return StoneyCoreForge.CONFIG.visual.noiseEffect;
            }

            @Override
            public int getMuzzlesSmokeParticlesTime() {
                return StoneyCoreForge.CONFIG.visual.muzzlesSmokeParticlesTime;
            }

            @Override
            public int getStaminaBarYOffset() {
                return StoneyCoreForge.CONFIG.visual.staminaBarYOffset;
            }

            @Override
            public int hexColorTooFarClose() {
                return StoneyCoreForge.CONFIG.visual.hexColorTooFarClose;
            }

            @Override
            public int hexColorEffective() {
                return StoneyCoreForge.CONFIG.visual.hexColorEffective;
            }

            @Override
            public int hexColorCritical() {
                return StoneyCoreForge.CONFIG.visual.hexColorCritical;
            }

            @Override
            public int hexColorMaximum() {
                return StoneyCoreForge.CONFIG.visual.hexColorMaximum;
            }

            @Override
            public boolean armWave() {
                return StoneyCoreForge.CONFIG.visual.armWave;
            }

            @Override
            public int claimOutlineColor() {
                return StoneyCoreForge.CONFIG.visual.claimOutlineColor;
            }
        };
    }

    @Override
    public TechnicalOptions technicalOptions() {
        return new TechnicalOptions() {
            @Override
            public int maxWorkPerTick() {
                return StoneyCoreForge.CONFIG.technical.maxWorkPerTick;
            }

            @Override
            public int maxLandExpandRadius() {
                return StoneyCoreForge.CONFIG.technical.maxLandExpandRadius;
            }

            @Override
            public boolean breakOrRemoveSiegeDestroy() {
                return StoneyCoreForge.CONFIG.technical.breakOrRemoveSiegeDestroy;
            }
        };
    }

    @Override
    public LandOptions landOptions() {
        return new LandOptions() {
            @Override
            public boolean claimLand() {
                return StoneyCoreForge.CONFIG.land.claimLand;
            }

            @Override
            public boolean removeClaimedSiege() {
                return StoneyCoreForge.CONFIG.land.removeClaimedSiege;
            }

            @Override
            public boolean hungerSiege() {
                return StoneyCoreForge.CONFIG.land.hungerSiege;
            }
        };
    }
}