package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCore;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = StoneyCore.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class SCConfigs extends PartitioningSerializer.GlobalData {

    @ConfigEntry.Category("combat")
    @ConfigEntry.Gui.TransitiveObject()
    public Combat combat = new Combat();

    @ConfigEntry.Category("visual")
    @ConfigEntry.Gui.TransitiveObject()
    public Visual visual = new Visual();

    @ConfigEntry.Category("technical")
    @ConfigEntry.Gui.TransitiveObject()
    public Technical technical = new Technical();

    @ConfigEntry.Category("land")
    @ConfigEntry.Gui.TransitiveObject()
    public Land land = new Land();

    /* ---------------- Combat ---------------- */

    @Config(name = StoneyCore.MOD_ID + "-combat")
    public static final class Combat implements ConfigData {

        @ConfigEntry.Gui.Tooltip()
        @Comment("Realistic Combat | Default: true")
        public boolean realisticCombat = true;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 99999)
        @Comment("Time (in ticks) to Toggle Visor | Default: 10")
        public int toggleVisorTime = 10;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Parry | Default: true")
        public boolean parry = true;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Max Base Stamina (0 or less disables) | Default: 20")
        public float maxBaseStamina = 20f;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Stamina Recovery Formula")
        public String staminaRecoveryFormula = "10 - (foodLevel + health) / 5";

        @ConfigEntry.Gui.Tooltip()
        @Comment("Blocking Stamina Constant")
        public double blockingStaminaConstant = 0.01d;

        @ConfigEntry.Gui.Tooltip()
        @Comment("On Block Stamina Constant")
        public double onBlockStaminaConstant = 0.03d;

        @ConfigEntry.Gui.Tooltip()
        @Comment("On Parry Stamina Constant")
        public double onParryStaminaConstant = 0.025d;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Sprinting Stamina Constant")
        public double sprintingStaminaConstant = 0.04d;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Jumping Stamina Constant")
        public double jumpingStaminaConstant = 0.01d;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Swimming Stamina Constant")
        public double swimmingStaminaConstant = 0.02d;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Attack Stamina Constant")
        public double attackStaminaConstant = 0.02d;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 99999)
        @Comment("Stamina Time to Replenish after Using | Default: 60")
        public int staminaRecoverTime = 60;
    }

    /* ---------------- Visual ---------------- */

    @Config(name = StoneyCore.MOD_ID + "-visual")
    public static final class Visual implements ConfigData {

        @ConfigEntry.Gui.Tooltip()
        @Comment("Damage Indicator | Default: false")
        public boolean damageIndicator = false;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Visored Helmet Overlay | Default: true")
        public boolean visoredHelmet = true;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        @Comment("Visored Helmet Overlay Alpha in Creative | Default: 0.4")
        public float visoredHelmetAlpha = 0.4f;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Low Stamina Indicator | Default: true")
        public boolean lowStaminaIndicator = true;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Noise Effect in Low Stamina Indicator | Default: true")
        public boolean noiseEffect = true;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Muzzles Smoke Particles Time Active (Seconds) | Default: 60")
        public int muzzlesSmokeParticlesTime = 60;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Stamina Bar Y Offset | Default: 0")
        public int staminaBarYOffset = 0;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Hex Color Too/Far Close")
        public int hexColorTooFarClose = 0xFFFFFF;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Hex Color Effective")
        public int hexColorEffective = 0xCBBD63;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Hex Color Critical")
        public int hexColorCritical = 0xFF4949;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Hex Color Maximum")
        public int hexColorMaximum = 0xFFFFFF;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Player waves arms when in land under siege and not participant")
        public boolean armWave = true;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Claim Outline Render Hex Color")
        public int claimOutlineColor = 0x00FFFF80;
    }

    /* ---------------- Technical ---------------- */

    @Config(name = StoneyCore.MOD_ID + "-technical")
    public static final class Technical implements ConfigData {

        @ConfigEntry.Gui.Tooltip()
        @Comment("""
                Max Work for Claim Worker per tick
                Lower numbers will slow the claim,
                but improve performance.
                """)
        public int maxWorkPerTick = 5000;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Max Land Expand Radius | Default: -1 (Unlimited)")
        public int maxLandExpandRadius = -1;

        @ConfigEntry.Gui.Tooltip()
        @Comment("""
                Break or Remove Blocks on siege destroy
                false = Break
                true = Remove
                """)
        public boolean breakOrRemoveSiegeDestroy = false;
    }

    /* ---------------- Land ---------------- */

    @Config(name = StoneyCore.MOD_ID + "-land")
    public static final class Land implements ConfigData {

        @ConfigEntry.Gui.Tooltip()
        @Comment("Claim Land | Default: true")
        public boolean claimLand = true;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Remove radius claim on defender death | Default: true")
        public boolean removeClaimedSiege = true;

        @ConfigEntry.Gui.Tooltip()
        @Comment("Hunger inside a Siege | Default: true")
        public boolean hungerSiege = true;
    }
}
