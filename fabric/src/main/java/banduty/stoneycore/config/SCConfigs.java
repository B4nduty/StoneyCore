package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCore;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = StoneyCore.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/oak_planks.png")
public class SCConfigs implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean getRealisticCombat = true;
    @ConfigEntry.Gui.Tooltip
    public int getToggleVisorTime = 10;
    @ConfigEntry.Gui.Tooltip
    public boolean getParry = true;
    @ConfigEntry.Gui.Tooltip
    public String staminaRecoveryFormula = "10 - (foodLevel + health) / 5";
    @ConfigEntry.Gui.Tooltip
    public double blockingStaminaConstant = 0.01d;
    @ConfigEntry.Gui.Tooltip
    public double onBlockStaminaConstant = 0.03d;
    @ConfigEntry.Gui.Tooltip
    public double onParryStaminaConstant = 0.025d;
    @ConfigEntry.Gui.Tooltip
    public double sprintingStaminaConstant = 0.04d;
    @ConfigEntry.Gui.Tooltip
    public double jumpingStaminaConstant = 0.01d;
    @ConfigEntry.Gui.Tooltip
    public double swimmingStaminaConstant = 0.02d;
    @ConfigEntry.Gui.Tooltip
    public double attackStaminaConstant = 0.02d;
    @ConfigEntry.Gui.Tooltip
    public int getStaminaRecoverTime = 60;
    @ConfigEntry.Gui.Tooltip
    public boolean disableStamina = false;

    @ConfigEntry.Gui.Tooltip
    public boolean getDamageIndicator = false;
    @ConfigEntry.Gui.Tooltip
    public boolean getVisoredHelmet = true;
    @ConfigEntry.Gui.Tooltip
    public float getVisoredHelmetAlphaCreative = 0.4f;
    @ConfigEntry.Gui.Tooltip
    public float getVisoredHelmetAlphaSurvival = 1.0f;
    @ConfigEntry.Gui.Tooltip
    public boolean getOverlayThirdPerson = true;
    @ConfigEntry.Gui.Tooltip
    public boolean getLowStaminaIndicator = true;
    @ConfigEntry.Gui.Tooltip
    public boolean getNoiseEffect = true;
    @ConfigEntry.Gui.Tooltip
    public int getMuzzlesSmokeParticlesTime = 60;
    @ConfigEntry.Gui.Tooltip
    public int getStaminaBarYOffset = 0;
    @ConfigEntry.Gui.Tooltip
    public String hexColorTooFarClose = "0xFFFFFF";
    @ConfigEntry.Gui.Tooltip
    public String claimOutlineColor = "0x00FFFF80";

    @ConfigEntry.Gui.Tooltip
    public int maxWorkPerTick = 5000;
    @ConfigEntry.Gui.Tooltip
    public int maxLandExpandRadius = -1;
    @ConfigEntry.Gui.Tooltip
    public boolean breakOrRemoveSiegeDestroy = false;

    @ConfigEntry.Gui.Tooltip
    public boolean claimLand = true;
    @ConfigEntry.Gui.Tooltip
    public boolean removeClaimedSiege = true;
    @ConfigEntry.Gui.Tooltip
    public boolean hungerSiege = true;
    @ConfigEntry.Gui.Tooltip
    public boolean landVisitors = false;
}