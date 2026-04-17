package banduty.stoneycore.config;

public class ForgeConfigImpl extends ConfigImpl {
    @Override
    public CombatOptions combatOptions() {
        return new CombatOptions() {
            @Override
            public boolean getRealisticCombat() {
                return SCConfigs.realisticCombat.get();
            }

            @Override
            public int getToggleVisorTime() {
                return SCConfigs.toggleVisorTime.get();
            }

            @Override
            public boolean getParry() {
                return SCConfigs.parry.get();
            }

            @Override
            public float maxBaseStamina() {
                return SCConfigs.maxBaseStamina.get().floatValue();
            }

            @Override
            public String staminaRecoveryFormula() {
                return SCConfigs.staminaRecoveryFormula.get();
            }

            @Override
            public double blockingStaminaConstant() {
                return SCConfigs.blockingStaminaConstant.get();
            }

            @Override
            public double onBlockStaminaConstant() {
                return SCConfigs.onBlockStaminaConstant.get();
            }

            @Override
            public double onParryStaminaConstant() {
                return SCConfigs.onParryStaminaConstant.get();
            }

            @Override
            public double sprintingStaminaConstant() {
                return SCConfigs.sprintingStaminaConstant.get();
            }

            @Override
            public double jumpingStaminaConstant() {
                return SCConfigs.jumpingStaminaConstant.get();
            }

            @Override
            public double swimmingStaminaConstant() {
                return SCConfigs.swimmingStaminaConstant.get();
            }

            @Override
            public double attackStaminaConstant() {
                return SCConfigs.attackStaminaConstant.get();
            }

            @Override
            public int getStaminaRecoverTime() {
                return SCConfigs.staminaRecoverTime.get();
            }
        };
    }

    @Override
    public VisualOptions visualOptions() {
        return new VisualOptions() {
            @Override
            public boolean getDamageIndicator() {
                return SCVisualConfigs.damageIndicator.get();
            }

            @Override
            public boolean getVisoredHelmet() {
                return SCVisualConfigs.visoredHelmet.get();
            }

            @Override
            public float getVisoredHelmetAlphaCreative() {
                return SCVisualConfigs.visoredHelmetAlphaCreative.get().floatValue();
            }

            @Override
            public float getVisoredHelmetAlphaSurvival() {
                return SCVisualConfigs.visoredHelmetAlphaSurvival.get().floatValue();
            }

            @Override
            public boolean overlayThirdPerson() {
                return SCVisualConfigs.overlayThirdPerson.get();
            }

            @Override
            public boolean getLowStaminaIndicator() {
                return SCVisualConfigs.lowStaminaIndicator.get();
            }

            @Override
            public boolean getNoiseEffect() {
                return SCVisualConfigs.noiseEffect.get();
            }

            @Override
            public int getMuzzlesSmokeParticlesTime() {
                return SCVisualConfigs.muzzlesSmokeParticlesTime.get();
            }

            @Override
            public int getStaminaBarYOffset() {
                return SCVisualConfigs.staminaBarYOffset.get();
            }

            @Override
            public int hexColorTooFarClose() {
                return hexToInt(SCVisualConfigs.hexColorTooFarClose.get());
            }

            @Override
            public int claimOutlineColor() {
                return hexToInt(SCVisualConfigs.claimOutlineColor.get());
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
    public TechnicalOptions technicalOptions() {
        return new TechnicalOptions() {
            @Override
            public int maxWorkPerTick() {
                return SCConfigs.maxWorkPerTick.get();
            }

            @Override
            public int maxLandExpandRadius() {
                return SCConfigs.maxLandExpandRadius.get();
            }

            @Override
            public boolean breakOrRemoveSiegeDestroy() {
                return SCConfigs.breakOrRemoveSiegeDestroy.get();
            }
        };
    }

    @Override
    public LandOptions landOptions() {
        return new LandOptions() {
            @Override
            public boolean claimLand() {
                return SCConfigs.claimLand.get();
            }

            @Override
            public boolean removeClaimedSiege() {
                return SCConfigs.removeClaimedSiege.get();
            }

            @Override
            public boolean hungerSiege() {
                return SCConfigs.hungerSiege.get();
            }

            @Override
            public boolean landVisitors() {
                return SCConfigs.landVisitors.get();
            }
        };
    }
}