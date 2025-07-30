package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCore;
import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@Modmenu(modId = StoneyCore.MOD_ID)
@Config(name = StoneyCore.MOD_ID, wrapperName = "StoneyCoreConfig")
public class SCConfigs {

    @Nest
    public CombatOptions combatOptions = new CombatOptions();

    public static class CombatOptions {
        @Comment("Realistic Combat")
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public boolean getRealisticCombat = true;

        @Comment("Parry")
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public boolean getParry = true;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Max Base Stamina (0 or less disables)")
        public float maxBaseStamina = 20f;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Stamina Recovery Formula")
        public String staminaRecoveryFormula = "10 - (foodLevel + health) / 5";

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Blocking Stamina Per Second")
        public float blockingStaminaPerSecond = 0.5f;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("On Block Stamina")
        public float onBlockStamina = 1.5f;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("On Parry Stamina")
        public float onParryStamina = 2.5f;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Sprinting Stamina Per Second")
        public float sprintingStaminaPerSecond = 2f;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Sprinting Stamina")
        public float jumpingStamina = 1f;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Swimming Stamina Per Second")
        public float swimmingStaminaPerSecond = 1f;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Attack Stamina")
        public float attackStamina = 2f;
    }

    @Nest
    public VisualOptions visualOptions = new VisualOptions();

    public static class VisualOptions {
        @Comment("Damage Indicator")
        public boolean getDamageIndicator = false;

        @Comment("Visored Helmet Overlay")
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public boolean getVisoredHelmet = true;

        @Comment("Low Stamina Indicator")
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public boolean getLowStaminaIndicator = true;

        @Comment("Noise Effect in Low Stamina Indicator")
        public boolean getNoiseEffect = true;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
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
    public TechnicalOptions technicalOptions = new TechnicalOptions();

    public static class TechnicalOptions {
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("""
                Max Work for Claim Worker per tick
                Lower numbers will make the claim slower,
                but it will "increase" the performance.
                """)
        public int maxWorkPerTick = 5000;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Max Land Expand Radius")
        public int maxLandExpandRadius = -1;
    }

    @Nest
    public LandOptions landOptions = new LandOptions();

    public static class LandOptions {
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Remove radius claim on defender death")
        public boolean removeClaimedSiege = true;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @Comment("Hunger inside a Siege")
        public boolean hungerSiege = true;
    }
}