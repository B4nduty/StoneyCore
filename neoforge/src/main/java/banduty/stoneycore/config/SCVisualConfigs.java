package banduty.stoneycore.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SCVisualConfigs {

    public static final ModConfigSpec SPEC;

    // Visual settings
    public static final ModConfigSpec.BooleanValue damageIndicator;
    public static final ModConfigSpec.BooleanValue visoredHelmet;
    public static final ModConfigSpec.DoubleValue visoredHelmetAlphaCreative;
    public static final ModConfigSpec.DoubleValue visoredHelmetAlphaSurvival;
    public static final ModConfigSpec.BooleanValue overlayThirdPerson;
    public static final ModConfigSpec.BooleanValue lowStaminaIndicator;
    public static final ModConfigSpec.BooleanValue noiseEffect;

    public static final ModConfigSpec.IntValue muzzlesSmokeParticlesTime;
    public static final ModConfigSpec.IntValue staminaBarYOffset;

    public static final ModConfigSpec.ConfigValue<String> hexColorTooFarClose;

    public static final ModConfigSpec.ConfigValue<String> claimOutlineColor;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        damageIndicator = builder
                .comment("Damage Indicator | Default: false")
                .define("damageIndicator", false);

        visoredHelmet = builder
                .comment("Visored Helmet Overlay | Default: true")
                .define("visoredHelmet", true);

        visoredHelmetAlphaCreative = builder
                .comment("Visored Helmet Overlay Alpha in Creative | Default: 0.4")
                .defineInRange("visoredHelmetAlphaCreative", 0.4, 0.0, 1.0);

        visoredHelmetAlphaSurvival = builder
                .comment("Visored Helmet Overlay Alpha in Survival | Default: 1.0")
                .defineInRange("visoredHelmetAlphaSurvival", 1.0, 0.0, 1.0);

        overlayThirdPerson = builder
                .comment("Visor Overlay being visible in Third Person | Default: true")
                .define("overlayThirdPerson", true);

        lowStaminaIndicator = builder
                .comment("Low Stamina Indicator | Default: true")
                .define("lowStaminaIndicator", true);

        noiseEffect = builder
                .comment("Noise Effect in Low Stamina Indicator | Default: true")
                .define("noiseEffect", true);

        muzzlesSmokeParticlesTime = builder
                .comment("Muzzles Smoke Particles Time Active (Seconds) | Default: 60")
                .defineInRange("muzzlesSmokeParticlesTime", 60, 0, Integer.MAX_VALUE);

        staminaBarYOffset = builder
                .comment("Stamina Bar Y Offset | Default: 0")
                .defineInRange("staminaBarYOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

        hexColorTooFarClose = builder
                .comment("Hex Color Too/Far Close")
                .define("hexColorTooFarClose", "0xFFFFFF");

        claimOutlineColor = builder
                .comment("Claim Outline Render Hex Color")
                .define("claimOutlineColor", "0x00FFFF80");

        SPEC = builder.build();
    }
}