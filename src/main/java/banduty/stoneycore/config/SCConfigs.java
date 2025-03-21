package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCore;
import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@Modmenu(modId = StoneyCore.MOD_ID)
@Config(name = StoneyCore.MOD_ID, wrapperName = "StoneyCoreConfig")
public class SCConfigs {
    @Comment("Use Stamina on or while Blocking")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean getBlocking = true;

    @Comment("Damage Indicator")
    public boolean getDamageIndicator = false;

    @Comment("Visored Helmet Overlay")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean getVisoredHelmet = true;

    @Sync(Option.SyncMode.NONE)
    @Comment("Low Stamina Indicator [Experimental]")
    public boolean getLowStaminaIndicator = false;

    @RestartRequired
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Comment("Muzzles Smoke Particles Time Active (In Seconds)")
    public int getMuzzlesSmokeParticlesTime = 60;

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Comment("Parry [Experimental]")
    public boolean getParry = false;

    @Sync(Option.SyncMode.NONE)
    @Comment("Stamina Bar Y Offset")
    public int getStaminaBarYOffset = 0;

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Comment("Max Stamina")
    public float maxStamina = 40f;

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