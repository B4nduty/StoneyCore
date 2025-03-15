package banduty.stoneycore.config;

import banduty.stoneycore.StoneyCore;
import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RestartRequired;
import io.wispforest.owo.config.annotation.Sync;

@Modmenu(modId = StoneyCore.MOD_ID)
@Config(name = StoneyCore.MOD_ID, wrapperName = "StoneyCoreConfig")
public class SCConfigs {
    @Comment("Vanilla Weapons deals 0 Damage")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean getVanillaWeaponsDamage0 = false;

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
}