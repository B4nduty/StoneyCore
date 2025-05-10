package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCore;
import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@Modmenu(modId = StoneyCore.MOD_ID)
@Config(name = StoneyCore.MOD_ID, wrapperName = "StoneyCoreConfig")
public class SCConfigs {
    @Comment("Realistic Combat")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean getRealisticCombat = true;

    @Comment("Damage Indicator")
    public boolean getDamageIndicator = false;

    @Comment("Visored Helmet Overlay")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean getVisoredHelmet = true;

    @Comment("Low Stamina Indicator")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean getLowStaminaIndicator = true;

    @RestartRequired
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Comment("Muzzles Smoke Particles Time Active (In Seconds)")
    public int getMuzzlesSmokeParticlesTime = 60;

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Comment("Parry")
    public boolean getParry = true;

    @Sync(Option.SyncMode.NONE)
    @Comment("Stamina Bar Y Offset")
    public int getStaminaBarYOffset = 0;

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Comment("""
            Max Stamina
            If set to 0, it will be disabled
            """)
    public float maxStamina = 20f;

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
}