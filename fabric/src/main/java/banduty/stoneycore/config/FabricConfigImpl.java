package banduty.stoneycore.config;

public class FabricConfigImpl extends ConfigImpl {
    private final banduty.stoneycore.config.StoneyCoreConfig config;

    public FabricConfigImpl() {
        this.config = banduty.stoneycore.config.StoneyCoreConfig.createAndLoad();
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
            public boolean overlayThirdPerson() {
                return config.visualOptions.getOverlayThirdPerson();
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
                return hexToInt(config.visualOptions.hexColorTooFarClose());
            }

            @Override
            public int claimOutlineColor() {
                return hexToInt(config.visualOptions.claimOutlineColor());
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

            @Override
            public boolean landVisitors() {
                return config.landOptions.landVisitors();
            }
        };
    }
}