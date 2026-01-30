package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCore;
import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@Modmenu(modId = StoneyCore.MOD_ID)
@Config(name = StoneyCore.MOD_ID, wrapperName = "StoneyCoreConfig")
public class SCConfigs {

    @Nest
    @SectionHeader("combatOptions")
    public CombatOptions combatOptions = new CombatOptions();

    public static class CombatOptions {
        @Comment("Realistic Combat")
        @Sync(Option.SyncMode.INFORM_SERVER)
        public boolean getRealisticCombat = true;

        @Comment("Time (In Ticks) to Toggle Visor")
        @RangeConstraint(min = 0, max = 99999)
        @Sync(Option.SyncMode.INFORM_SERVER)
        public int getToggleVisorTime = 10;

        @Comment("Parry")
        @Sync(Option.SyncMode.INFORM_SERVER)
        public boolean getParry = true;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Max Base Stamina (0 or less disables)")
        public float maxBaseStamina = 20f;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Stamina Recovery Formula")
        public String staminaRecoveryFormula = "10 - (foodLevel + health) / 5";

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Blocking Stamina Constant")
        public double blockingStaminaConstant = 0.01d;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("On Block Stamina Constant")
        public double onBlockStaminaConstant = 0.03d;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("On Parry Stamina Constant")
        public double onParryStaminaConstant = 0.025d;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Sprinting Stamina Constant")
        public double sprintingStaminaConstant = 0.04d;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Jumping Stamina Constant")
        public double jumpingStaminaConstant = 0.01d;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Swimming Stamina Constant")
        public double swimmingStaminaConstant = 0.02d;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Attack Stamina Constant")
        public double attackStaminaConstant = 0.02d;

        @Comment("Stamina Time to Replenish after Using")
        @RangeConstraint(min = 0, max = 99999)
        @Sync(Option.SyncMode.INFORM_SERVER)
        public int getStaminaRecoverTime = 60;
    }

    @Nest
    @SectionHeader("visualOptions")
    public VisualOptions visualOptions = new VisualOptions();

    public static class VisualOptions {
        @Comment("Damage Indicator")
        public boolean getDamageIndicator = false;

        @Comment("Visored Helmet Overlay")
        @Sync(Option.SyncMode.INFORM_SERVER)
        public boolean getVisoredHelmet = true;

        @Comment("Visored Helmet Overlay Alpha in Creative")
        @RangeConstraint(min = 0f, max = 1f)
        public float getVisoredHelmetAlphaCreative = 0.4f;

        @Comment("Visored Helmet Overlay Alpha in Survival")
        @RangeConstraint(min = 0f, max = 1f)
        public float getVisoredHelmetAlphaSurvival = 1.0f;

        @Comment("Low Stamina Indicator")
        @Sync(Option.SyncMode.INFORM_SERVER)
        public boolean getLowStaminaIndicator = true;

        @Comment("Noise Effect in Low Stamina Indicator")
        public boolean getNoiseEffect = true;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Muzzles Smoke Particles Time Active (In Seconds)")
        public int getMuzzlesSmokeParticlesTime = 60;

        @Comment("Stamina Bar Y Offset")
        @Sync(Option.SyncMode.NONE)
        public int getStaminaBarYOffset = 0;

        @Sync(Option.SyncMode.NONE)
        @Comment("Hex Color Too/Far Close")
        public int hexColorTooFarClose = 0xFFFFFF;

        @Sync(Option.SyncMode.NONE)
        @Comment("Hex Color Effective")
        public int hexColorEffective = 0xcbbd63;

        @Sync(Option.SyncMode.NONE)
        @Comment("Hex Color Critical")
        public int hexColorCritical = 0xff4949;

        @Sync(Option.SyncMode.NONE)
        @Comment("Hex Color Maximum")
        public int hexColorMaximum = 0xFFFFFF;

        @Sync(Option.SyncMode.NONE)
        @Comment("Player waves arms when in land under siege and not participant")
        public boolean armWave = true;

        @Sync(Option.SyncMode.NONE)
        @Comment("Claim Outline Render Hex Color")
        public int claimOutlineColor = 0x00FFFF80;
    }

    @Nest
    @SectionHeader("technicalOptions")
    public TechnicalOptions technicalOptions = new TechnicalOptions();
    
    public static class TechnicalOptions {
        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("""
                Max Work for Claim Worker per tick
                Lower numbers will make the claim slower,
                but it will "increase" the performance.
                """)
        public int maxWorkPerTick = 5000;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Max Land Expand Radius")
        public int maxLandExpandRadius = -1;
        
        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("""
            Break or Remove Blocks on destroy with siege engines
            Break = False
            Remove = True
            """)
        public boolean breakOrRemoveSiegeDestroy = false;
    }

    @Nest
    @SectionHeader("landOptions")
    public LandOptions landOptions = new LandOptions();

    public static class LandOptions {
        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Claim Land")
        public boolean claimLand = true;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Remove radius claim on defender death")
        public boolean removeClaimedSiege = true;

        @Sync(Option.SyncMode.INFORM_SERVER)
        @Comment("Hunger inside a Siege")
        public boolean hungerSiege = true;
    }
}