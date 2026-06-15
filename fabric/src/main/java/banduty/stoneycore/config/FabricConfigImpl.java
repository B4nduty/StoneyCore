package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCoreFabric;

public class FabricConfigImpl extends ConfigImpl {
    @Override
    public IConfig.CombatOptions combatOptions() {
        return new IConfig.CombatOptions() {
            @Override
            public boolean getRealisticCombat() {
                return StoneyCoreFabric.CONFIG.getRealisticCombat;
            }

            @Override
            public int getToggleVisorTime() {
                return StoneyCoreFabric.CONFIG.getToggleVisorTime;
            }

            @Override
            public boolean getParry() {
                return StoneyCoreFabric.CONFIG.getParry;
            }

            @Override
            public String staminaRecoveryFormula() {
                return StoneyCoreFabric.CONFIG.staminaRecoveryFormula;
            }

            @Override
            public double blockingStaminaConstant() {
                return StoneyCoreFabric.CONFIG.blockingStaminaConstant;
            }

            @Override
            public double onBlockStaminaConstant() {
                return StoneyCoreFabric.CONFIG.onBlockStaminaConstant;
            }

            @Override
            public double onParryStaminaConstant() {
                return StoneyCoreFabric.CONFIG.onParryStaminaConstant;
            }

            @Override
            public double sprintingStaminaConstant() {
                return StoneyCoreFabric.CONFIG.sprintingStaminaConstant;
            }

            @Override
            public double jumpingStaminaConstant() {
                return StoneyCoreFabric.CONFIG.jumpingStaminaConstant;
            }

            @Override
            public double swimmingStaminaConstant() {
                return StoneyCoreFabric.CONFIG.swimmingStaminaConstant;
            }

            @Override
            public double attackStaminaConstant() {
                return StoneyCoreFabric.CONFIG.attackStaminaConstant;
            }

            @Override
            public int getStaminaRecoverTime() {
                return StoneyCoreFabric.CONFIG.getStaminaRecoverTime;
            }

            @Override
            public boolean disableStamina() {
                return StoneyCoreFabric.CONFIG.disableStamina;
            }
        };
    }

    @Override
    public IConfig.VisualOptions visualOptions() {
        return new IConfig.VisualOptions() {
            @Override
            public boolean getDamageIndicator() {
                return StoneyCoreFabric.CONFIG.getDamageIndicator;
            }

            @Override
            public boolean getVisoredHelmet() {
                return StoneyCoreFabric.CONFIG.getVisoredHelmet;
            }

            @Override
            public float getVisoredHelmetAlphaCreative() {
                return StoneyCoreFabric.CONFIG.getVisoredHelmetAlphaCreative;
            }

            @Override
            public float getVisoredHelmetAlphaSurvival() {
                return StoneyCoreFabric.CONFIG.getVisoredHelmetAlphaSurvival;
            }

            @Override
            public boolean overlayThirdPerson() {
                return StoneyCoreFabric.CONFIG.getOverlayThirdPerson;
            }

            @Override
            public boolean getLowStaminaIndicator() {
                return StoneyCoreFabric.CONFIG.getLowStaminaIndicator;
            }

            @Override
            public boolean getNoiseEffect() {
                return StoneyCoreFabric.CONFIG.getNoiseEffect;
            }

            @Override
            public int getMuzzlesSmokeParticlesTime() {
                return StoneyCoreFabric.CONFIG.getMuzzlesSmokeParticlesTime;
            }

            @Override
            public int getStaminaBarYOffset() {
                return StoneyCoreFabric.CONFIG.getStaminaBarYOffset;
            }

            @Override
            public int hexColorTooFarClose() {
                return hexToInt(StoneyCoreFabric.CONFIG.hexColorTooFarClose);
            }

            @Override
            public int claimOutlineColor() {
                return hexToInt(StoneyCoreFabric.CONFIG.claimOutlineColor);
            }
        };
    }

    public static int hexToInt(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex string cannot be null");
        }

        hex = hex.trim();

        // Remove optional prefixes
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        } else if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        return Integer.parseUnsignedInt(hex, 16);
    }

    @Override
    public IConfig.TechnicalOptions technicalOptions() {
        return new IConfig.TechnicalOptions() {
            @Override
            public int maxWorkPerTick() {
                return StoneyCoreFabric.CONFIG.maxWorkPerTick;
            }

            @Override
            public int maxLandExpandRadius() {
                return StoneyCoreFabric.CONFIG.maxLandExpandRadius;
            }

            @Override
            public boolean breakOrRemoveSiegeDestroy() {
                return StoneyCoreFabric.CONFIG.breakOrRemoveSiegeDestroy;
            }
        };
    }

    @Override
    public IConfig.LandOptions landOptions() {
        return new IConfig.LandOptions() {
            @Override
            public boolean claimLand() {
                return StoneyCoreFabric.CONFIG.claimLand;
            }

            @Override
            public boolean removeClaimedSiege() {
                return StoneyCoreFabric.CONFIG.removeClaimedSiege;
            }

            @Override
            public boolean hungerSiege() {
                return StoneyCoreFabric.CONFIG.hungerSiege;
            }

            @Override
            public boolean landVisitors() {
                return StoneyCoreFabric.CONFIG.landVisitors;
            }
        };
    }
}